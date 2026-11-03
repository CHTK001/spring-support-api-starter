package com.chua.agent;

import com.sun.jna.Native;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinNT.HANDLE;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * NTFS MFT 直读扫描器（WizTree 原理）。
 *
 * <p>Windows 上以 SYSTEM 身份打开卷设备 {@code \\.\X:}，解析引导扇区得到 $MFT 位置与记录大小，
 * 再按 $MFT 的 data runs 顺序读取全部 FILE 记录，从 $FILE_NAME / $DATA 中提取父子关系与
 * 分配大小，自底向上聚合目录体积。相比 {@code File.listFiles()} 递归，能看到受 ACL 保护、
 * 普通遍历无法枚举的文件（目标机上这部分可达约 90GB）。</p>
 *
 * <p>本类自包含、防御式实现：任何一步失败（非 Windows / 非 NTFS / 权限不足 / 解析异常），
 * {@link #scan(String)} 都返回 {@code null}，由调用方回退到普通递归遍历，不影响既有功能。</p>
 *
 * <p>字节码钉在 Java 8，仅使用 JNA（Agent 已随 OSHI 打包）。</p>
 *
 * @author CH
 * @since 4.0.0.51
 */
final class NtfsMftReader {

    private static final long MFT_ROOT_FRN = 5L;
    private static final long FILETIME_EPOCH_DELTA = 116444736000000000L;
    private static final int TOP_FILES = 12;
    /**
     * 日志
     */
    private static final java.util.logging.Logger LOG =
            java.util.logging.Logger.getLogger(NtfsMftReader.class.getName());

    /** Seek 原点：FILE_BEGIN */
    private static final int FILE_BEGIN = 0;

    /**
     * 仅补充 Kernel32 未声明的 SetFilePointerEx（系统函数签名稳定）。
     */
    private interface VolumeApi extends StdCallLibrary {
        VolumeApi INSTANCE = Native.load("kernel32", VolumeApi.class);

        boolean SetFilePointerEx(HANDLE h, long distanceToMove,
                                 com.sun.jna.ptr.LongByReference newFilePointer, int moveMethod);
    }

    private NtfsMftReader() {
    }

    // ==================== 对外入口 ====================

    /**
     * 扫描指定盘符。
     *
     * @param drivePath 卷根路径，形如 {@code C:/}、{@code C:\}、{@code C:}
     * @return 与 walkUsageLocal 同构的占用节点；失败返回 null
     */
    static Map<String, Object> scan(String drivePath) {
        char letter = driveLetter(drivePath);
        if (letter == 0) {
            return fail("no drive letter in: " + drivePath);
        }
        HANDLE handle = null;
        try {
            String device = "\\\\.\\" + letter + ":";
            handle = Kernel32.INSTANCE.CreateFile(
                    device,
                    WinNT.GENERIC_READ,
                    WinNT.FILE_SHARE_READ | WinNT.FILE_SHARE_WRITE,
                    null,
                    WinNT.OPEN_EXISTING,
                    0,
                    null);
            if (handle == null || !isValid(handle)) {
                LOG.severe("[mft] open fail device=" + device + " err=" + Native.getLastError());
                return null;
            }

            // 1) 引导扇区
            byte[] boot = readVolume(handle, 0L, 8192);
            if (boot == null || boot.length < 512) {
                return fail("boot read null/short, len=" + (boot == null ? -1 : boot.length));
            }
            if (!(boot[3] == 'N' && boot[4] == 'T' && boot[5] == 'F' && boot[6] == 'S')) {
                return fail("not NTFS, head=" + hex(boot, 16));
            }
            int bps = u16(boot, 0x0B);
            int spc = u8(boot, 0x0D);
            if (bps < 512 || spc < 1) {
                return fail("bad bps/spc: bps=" + bps + " spc=" + spc);
            }
            int clusterBytes = bps * spc;
            long mftLcn = i64(boot, 0x30);
            // 0x40 是单字节有符号描述：负数→2^(-x) 字节（典型 0xF6=-10→1024），正数→每记录簇数
            int recDesc = boot[0x40];
            int recordSize = recDesc < 0
                    ? (int) pow2(-recDesc)
                    : recDesc * clusterBytes;
            if (recordSize < 256 || recordSize > 16384) {
                return fail("bad recordSize=" + recordSize + " recDesc=" + recDesc);
            }

            // 2) 记录 0（$MFT）→ data runs
            long mftPhys = mftLcn * (long) clusterBytes;
            byte[] rec0 = readVolume(handle, mftPhys, Math.max(clusterBytes, recordSize) * 2);
            List<Run> runs = parseMftRuns(rec0, bps, clusterBytes);
            if (runs == null || runs.isEmpty()) {
                return fail("mft runs empty, rec0len=" + rec0.length);
            }
            long totalMftBytes = 0L;
            for (Run r : runs) {
                totalMftBytes += r.clusters * (long) clusterBytes;
            }
            int n = (int) ((totalMftBytes + recordSize - 1) / recordSize);
            LOG.severe("[mft] runs=" + runs.size() + " n=" + n + " totalMftBytes=" + totalMftBytes);

            // 每记录数组
            long[] parent = new long[n];
            long[] ownAlloc = new long[n];
            long[] ownReal = new long[n];
            long[] sumAlloc = new long[n];
            long[] sumReal = new long[n];
            long[] modFt = new long[n];
            String[] names = new String[n];
            byte[] state = new byte[n]; // bit0 inUse, bit1 dir
            long[] fileCnt = new long[n];
            long[] dirCnt = new long[n];

            // 3) 顺序读取各 run，解析记录
            long logicalPos = 0L;
            byte[] carry = new byte[0];
            for (Run run : runs) {
                int runBytes = (int) (run.clusters * (long) clusterBytes);
                long phys = run.lcn * (long) clusterBytes;
                byte[] block = readVolume(handle, phys, runBytes);
                if (block == null) {
                    return fail("run block read null phys=" + phys + " runBytes=" + runBytes);
                }
                byte[] data = new byte[carry.length + block.length];
                System.arraycopy(carry, 0, data, 0, carry.length);
                System.arraycopy(block, 0, data, carry.length, block.length);
                long dataStart = logicalPos - carry.length;
                int consumed = 0;
                while (data.length - consumed >= recordSize) {
                    long frn = (dataStart + consumed) / recordSize;
                    if (frn >= 0 && frn < n) {
                        parseRecord(data, consumed, recordSize, bps, clusterBytes, frn, parent,
                                ownAlloc, ownReal, modFt, names, state);
                    }
                    consumed += recordSize;
                }
                carry = new byte[data.length - consumed];
                System.arraycopy(data, consumed, carry, 0, carry.length);
                logicalPos += runBytes;
            }

            // 4) 规范化父 FRN（孤儿挂根；根 5 指向自身）
            for (int frn = 0; frn < n; frn++) {
                if ((state[frn] & 1) == 0) {
                    continue;
                }
                if (frn == MFT_ROOT_FRN) {
                    parent[frn] = MFT_ROOT_FRN;
                    continue;
                }
                long p = parent[frn] & 0xFFFFFFFFFFFFL;
                if (p == frn || p >= n || (state[(int) p] & 1) == 0) {
                    parent[frn] = MFT_ROOT_FRN;
                } else {
                    parent[frn] = p;
                }
            }

            // 5) 聚合：文件体积累加到全部祖先；目录计数同理
            for (int frn = 0; frn < n; frn++) {
                if ((state[frn] & 1) == 0) {
                    continue;
                }
                boolean isDir = (state[frn] & 2) != 0;
                if (!isDir) {
                    long a = ownAlloc[frn];
                    long r = ownReal[frn];
                    long p = parent[frn];
                    int guard = 0;
                    while (p != frn && guard++ < 512) {
                        int pi = (int) p;
                        sumAlloc[pi] += a;
                        sumReal[pi] += r;
                        fileCnt[pi]++;
                        if (p == MFT_ROOT_FRN) {
                            break;
                        }
                        p = parent[pi];
                    }
                }
            }
            // 目录→祖先 的目录计数
            for (int frn = 0; frn < n; frn++) {
                if ((state[frn] & 1) == 0 || (state[frn] & 2) == 0 || frn == MFT_ROOT_FRN) {
                    continue;
                }
                long p = parent[frn];
                int guard = 0;
                while (p != frn && guard++ < 512) {
                    dirCnt[(int) p]++;
                    if (p == MFT_ROOT_FRN) {
                        break;
                    }
                    p = parent[(int) p];
                }
            }

            // 6) 邻接表（用于输出）
            ArrayList<Integer>[] childAdj = new ArrayList[n];
            for (int frn = 0; frn < n; frn++) {
                if ((state[frn] & 1) == 0 || frn == MFT_ROOT_FRN) {
                    continue;
                }
                int p = (int) parent[frn];
                if (childAdj[p] == null) {
                    childAdj[p] = new ArrayList<Integer>();
                }
                childAdj[p].add(frn);
            }

            // 7) 构建输出树（受 depth 限制，但每个节点 size 始终为完整子树）
            int outDepth = 6;
            Map<String, Object> root = buildNode((int) MFT_ROOT_FRN, outDepth,
                    letter + ":/", childAdj, state, ownAlloc, names, sumAlloc, sumReal, fileCnt, dirCnt, modFt);
            return root;
        } catch (Throwable t) {
            LOG.log(java.util.logging.Level.WARNING, "NTFS MFT 扫描失败，回退普通遍历", t);
            return null;
        } finally {
            if (handle != null) {
                try {
                    Kernel32.INSTANCE.CloseHandle(handle);
                } catch (Throwable closeEx) {
                    LOG.log(java.util.logging.Level.FINE, "关闭句柄失败，已忽略", closeEx);
                }
            }
        }
    }

    // ==================== 输出构建 ====================

    private static Map<String, Object> buildNode(int frn, int remain, String path,
                                                 ArrayList<Integer>[] childAdj, byte[] state,
                                                 long[] ownAlloc, String[] names,
                                                 long[] sumAlloc, long[] sumReal,
                                                 long[] fileCnt, long[] dirCnt, long[] modFt) {
        boolean isDir = (state[frn] & 2) != 0;
        Map<String, Object> node = new LinkedHashMap<String, Object>();
        node.put("directory", isDir);
        node.put("path", path);
        String nm;
        if (frn == MFT_ROOT_FRN) {
            nm = path;
        } else {
            nm = names[frn];
            if (nm == null || nm.isEmpty()) {
                nm = "#" + frn;
            }
        }
        node.put("name", nm);
        node.put("size", isDir ? sumAlloc[frn] : ownAlloc[frn]);
        node.put("fileCount", isDir ? fileCnt[frn] : 1L);
        node.put("dirCount", isDir ? dirCnt[frn] : 0L);
        node.put("modified", modTimeString(modFt[frn]));

        List<Integer> children = childAdj[frn];
        if (isDir && remain > 0 && children != null && !children.isEmpty()) {
            List<Integer> dirs = new ArrayList<Integer>();
            List<Integer> files = new ArrayList<Integer>();
            for (int c : children) {
                if ((state[c] & 2) != 0) {
                    dirs.add(c);
                } else {
                    files.add(c);
                }
            }
            sortBySize(dirs, sumAlloc);
            sortBySize(files, ownAlloc);

            List<Map<String, Object>> out = new ArrayList<Map<String, Object>>();
            for (int d : dirs) {
                out.add(buildNode(d, remain - 1, path + (path.endsWith("/") ? "" : "/") + names[d],
                        childAdj, state, ownAlloc, names, sumAlloc, sumReal, fileCnt, dirCnt, modFt));
            }
            long restSize = 0L;
            long restCount = 0L;
            for (int i = 0; i < files.size(); i++) {
                int f = files.get(i);
                if (i < TOP_FILES) {
                    out.add(buildNode(f, remain - 1,
                            path + (path.endsWith("/") ? "" : "/") + names[f],
                            childAdj, state, ownAlloc, names, sumAlloc, sumReal, fileCnt, dirCnt, modFt));
                } else {
                    restSize += ownAlloc[f];
                    restCount++;
                }
            }
            if (restCount > 0) {
                Map<String, Object> rest = new LinkedHashMap<String, Object>();
                rest.put("directory", false);
                rest.put("name", "__files__");
                rest.put("path", path);
                rest.put("size", restSize);
                rest.put("fileCount", restCount);
                rest.put("dirCount", 0L);
                rest.put("modified", "");
                out.add(rest);
            }
            if (!out.isEmpty()) {
                node.put("children", out);
            }
        }
        return node;
    }

    private static void sortBySize(List<Integer> list, final long[] sizeArr) {
        list.sort(new java.util.Comparator<Integer>() {
            @Override
            public int compare(Integer a, Integer b) {
                return Long.compare(sizeArr[b], sizeArr[a]);
            }
        });
    }

    // ==================== MFT 记录解析 ====================

    private static void parseRecord(byte[] buf, int base, int recordSize, int bps, int clusterBytes, long frn,
                                    long[] parent, long[] ownAlloc, long[] ownReal,
                                    long[] modFt, String[] names, byte[] state) {
        int p0 = base;
        if (!(buf[p0] == 'F' && buf[p0 + 1] == 'I' && buf[p0 + 2] == 'L' && buf[p0 + 3] == 'E')) {
            return;
        }
        // USA fixup
        int usaOff = u16(buf, p0 + 0x04);
        int usaCount = u16(buf, p0 + 0x06);
        for (int i = 1; i < usaCount; i++) {
            int sectorPos = i * bps - 2;
            int src = usaOff + i * 2;
            if (sectorPos + 1 < recordSize && src + 1 < recordSize) {
                buf[p0 + sectorPos] = buf[p0 + src];
                buf[p0 + sectorPos + 1] = buf[p0 + src + 1];
            }
        }
        int flags = u16(buf, p0 + 0x16);
        boolean inUse = (flags & 0x0001) != 0;
        boolean isDir = (flags & 0x0002) != 0;
        if (!inUse) {
            return;
        }
        state[(int) frn] = (byte) ((isDir ? 2 : 0) | 1);

        // 若为扩展记录，$DATA 归属 base
        long baseRef = u64(buf, p0 + 0x20) & 0xFFFFFFFFFFFFL;
        long sizeOwner = (baseRef != 0 && baseRef != frn && baseRef < ownAlloc.length) ? baseRef : frn;

        int attrStart = u16(buf, p0 + 0x14);
        int ap = p0 + attrStart;
        int bestNs = -1;
        long bestParent = 0L;
        String bestName = null;
        long bestMod = 0L;
        long streamAlloc = 0L;
        long streamReal = 0L;
        boolean gotStream = false;

        while (ap + 8 <= buf.length && ap - p0 < recordSize) {
            long type = u32(buf, ap);
            if (type == 0xFFFFFFFFL) {
                break;
            }
            int len = (int) u32(buf, ap + 4);
            if (len <= 0 || ap + len > buf.length || ap - p0 + len > recordSize) {
                break;
            }
            int nonRes = u8(buf, ap + 8);
            int nameLen = u8(buf, ap + 9);

            if (type == 0x10L && nonRes == 0) {
                int co = u16(buf, ap + 0x14);
                bestMod = u64(buf, ap + co + 0x08); // 修改时间 FILETIME
            } else if (type == 0x30L && nonRes == 0) {
                int co = u16(buf, ap + 0x14);
                int c = ap + co;
                long par = u64(buf, c) & 0xFFFFFFFFFFFFL;
                int fnLen = u8(buf, c + 0x40);
                int fnNs = u8(buf, c + 0x41);
                String fn = utf16(buf, c + 0x42, fnLen);
                // 命名空间优先级：3(Win32+DOS) > 1(Win32) > 0(POSIX)；2(DOS) 最低
                int rank = fnNs == 3 ? 3 : fnNs == 1 ? 2 : fnNs == 0 ? 1 : 0;
                if (rank > bestNs && fn != null && !fn.isEmpty()) {
                    bestNs = rank;
                    bestParent = par;
                    bestName = fn;
                }
            } else if (type == 0x80L && nameLen == 0) {
                if (nonRes == 0) {
                    int valLen = (int) u32(buf, ap + 0x10);
                    streamAlloc = valLen;
                    streamReal = valLen;
                    gotStream = true;
                } else {
                    // 真实占盘：直接汇总 run list 中实际落盘的簇（稀疏空洞/压缩零段编码为
                    // offsetNib=0，跳过），对普通/压缩/稀疏文件都准确；0x28 对稀疏文件会把
                    // 名义空洞也计入，不能直接用。
                    streamAlloc = allocatedFromRuns(buf, ap, clusterBytes);
                    streamReal = u64(buf, ap + 0x30);
                    gotStream = true;
                }
            }
            ap += len;
        }

        if (bestName != null) {
            names[(int) frn] = bestName;
            parent[(int) frn] = bestParent;
        }
        if (bestMod != 0L) {
            modFt[(int) frn] = bestMod;
        }
        if (gotStream && !isDir) {
            int so = (int) sizeOwner;
            ownAlloc[so] += streamAlloc;
            ownReal[so] += streamReal;
        }
    }

    // ==================== $MFT run list ====================

    private static List<Run> parseMftRuns(byte[] rec0, int bps, int clusterBytes) {
        int p0 = 0;
        if (!(rec0[0] == 'F' && rec0[1] == 'I' && rec0[2] == 'L' && rec0[3] == 'E')) {
            return null;
        }
        int usaOff = u16(rec0, 0x04);
        int usaCount = u16(rec0, 0x06);
        for (int i = 1; i < usaCount; i++) {
            int sectorPos = i * bps - 2;
            int src = usaOff + i * 2;
            if (sectorPos + 1 < rec0.length && src + 1 < rec0.length) {
                rec0[sectorPos] = rec0[src];
                rec0[sectorPos + 1] = rec0[src + 1];
            }
        }
        int attrStart = u16(rec0, 0x14);
        int ap = attrStart;
        while (ap + 8 < rec0.length) {
            long type = u32(rec0, ap);
            if (type == 0xFFFFFFFFL) {
                break;
            }
            int len = (int) u32(rec0, ap + 4);
            if (len <= 0 || ap + len > rec0.length) {
                break;
            }
            int nonRes = u8(rec0, ap + 8);
            int nameLen = u8(rec0, ap + 9);
            if (type == 0x80L && nameLen == 0 && nonRes == 1) {
                int runOff = u16(rec0, ap + 0x20);
                return decodeRuns(rec0, ap + runOff);
            }
            ap += len;
        }
        return null;
    }

    private static List<Run> decodeRuns(byte[] buf, int p) {
        List<Run> runs = new ArrayList<Run>();
        long curLcn = 0L;
        long curVcn = 0L;
        while (p < buf.length) {
            int hdr = u8(buf, p++);
            if (hdr == 0) {
                break;
            }
            int lenBytes = hdr & 0x0F;
            int offBytes = (hdr >> 4) & 0x0F;
            long clusters = readSized(buf, p, lenBytes, false);
            p += lenBytes;
            long delta = readSized(buf, p, offBytes, true);
            p += offBytes;
            curLcn += delta;
            if (clusters > 0 && curLcn >= 0) {
                runs.add(new Run(curVcn, curLcn, clusters));
                curVcn += clusters;
            }
        }
        return runs;
    }

    /**
     * 汇总非驻留属性 run list 中实际落盘的字节。
     *
     * <p>每个 run 头低半字节为「长度字段字节数」，高半字节为「偏移字段字节数」。
     * 稀疏文件的空洞、压缩文件的全零压缩单元，其偏移字节数为 0（不落盘），跳过其长度；
     * 其余 run 的簇数累加后乘每簇字节数，即为真实占盘大小。</p>
     */
    private static long allocatedFromRuns(byte[] buf, int ap, int clusterBytes) {
        int runOff = u16(buf, ap + 0x20);
        if (runOff == 0) {
            return 0L;
        }
        int p = ap + runOff;
        int end = ap + u16(buf, ap + 4); // 属性总长，作为越界兜底
        long clusters = 0L;
        while (p < end && p < buf.length) {
            int hdr = u8(buf, p++);
            if (hdr == 0) {
                break;
            }
            int lenNib = hdr & 0x0F;
            int offNib = (hdr >> 4) & 0x0F;
            long len = readSized(buf, p, lenNib, false);
            p += lenNib;
            if (offNib > 0) {
                clusters += len; // 有物理起始簇 → 真实落盘
            }
            p += offNib; // 跳过偏移字段（空洞时为 0）
        }
        return clusters * (long) clusterBytes;
    }

    private static long readSized(byte[] buf, int p, int bytes, boolean signed) {
        long v = 0L;
        for (int i = 0; i < bytes; i++) {
            if (p + i < buf.length) {
                v |= ((long) buf[p + i] & 0xFFL) << (8 * i);
            }
        }
        if (signed && bytes > 0) {
            int shift = (8 - bytes) * 8;
            v = (v << shift) >> shift; // 符号扩展
        }
        return v;
    }

    // ==================== 卷读取（JNA） ====================

    private static byte[] readVolume(HANDLE handle, long offset, int len) {
        if (!VolumeApi.INSTANCE.SetFilePointerEx(handle, offset, null, FILE_BEGIN)) {
            LOG.severe("[mft] seek fail offset=" + offset + " err=" + Native.getLastError());
            return null;
        }
        byte[] buf = new byte[len];
        IntByReference read = new IntByReference(0);
        if (!Kernel32.INSTANCE.ReadFile(handle, buf, len, read, null)) {
            // 读到卷末尾附近可能失败并返回部分字节，按已读长度处理；否则视为失败
            if (read.getValue() <= 0) {
                LOG.severe("[mft] read fail offset=" + offset + " len=" + len
                        + " err=" + Native.getLastError());
                return null;
            }
        }
        int got = read.getValue();
        if (got <= 0) {
            return null;
        }
        if (got == len) {
            return buf;
        }
        byte[] trimmed = new byte[got];
        System.arraycopy(buf, 0, trimmed, 0, got);
        return trimmed;
    }

    private static boolean isValid(HANDLE handle) {
        HANDLE invalid = WinBase.INVALID_HANDLE_VALUE;
        return handle.getPointer() != null
                && (invalid == null || !handle.getPointer().equals(invalid.getPointer()));
    }

    private static Map<String, Object> fail(String why) {
        LOG.severe("[mft] " + why);
        return null;
    }

    // ==================== 工具 ====================

    private static char driveLetter(String path) {
        if (path == null) {
            return 0;
        }
        String t = path.trim();
        for (int i = 0; i + 1 < t.length(); i++) {
            char c = t.charAt(i);
            if (((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')) && t.charAt(i + 1) == ':') {
                return Character.toUpperCase(c);
            }
        }
        return 0;
    }

    private static long pow2(int exp) {
        long v = 1L;
        for (int i = 0; i < exp; i++) {
            v <<= 1;
        }
        return v;
    }

    private static String modTimeString(long filetime) {
        if (filetime <= 0L) {
            return "";
        }
        long ms = (filetime - FILETIME_EPOCH_DELTA) / 10000L;
        if (ms <= 0L) {
            return "";
        }
        return new java.util.Date(ms).toString();
    }

    private static String utf16(byte[] buf, int p, int chars) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < chars; i++) {
            int lo = u8(buf, p + i * 2);
            int hi = u8(buf, p + i * 2 + 1);
            sb.append((char) ((hi << 8) | lo));
        }
        return sb.toString();
    }

    private static String hex(byte[] buf, int n) {
        StringBuilder sb = new StringBuilder();
        int m = Math.min(n, buf.length);
        for (int i = 0; i < m; i++) {
            sb.append(String.format("%02X ", buf[i] & 0xFF));
        }
        return sb.toString().trim();
    }

    private static int u8(byte[] buf, int p) {
        return p >= 0 && p < buf.length ? buf[p] & 0xFF : 0;
    }

    private static int u16(byte[] buf, int p) {
        return u8(buf, p) | (u8(buf, p + 1) << 8);
    }

    private static long u32(byte[] buf, int p) {
        return u8(buf, p) | (u8(buf, p + 1) << 8) | (u8(buf, p + 2) << 16) | ((long) u8(buf, p + 3) << 24);
    }

    private static long i64(byte[] buf, int p) {
        return u64(buf, p);
    }

    private static long u64(byte[] buf, int p) {
        long v = 0L;
        for (int i = 0; i < 8; i++) {
            v |= ((long) u8(buf, p + i)) << (8 * i);
        }
        return v;
    }

    /** data run：起始 VCN、物理 LCN、连续簇数 */
    private static final class Run {
        final long startVcn;
        final long lcn;
        final long clusters;

        Run(long startVcn, long lcn, long clusters) {
            this.startVcn = startVcn;
            this.lcn = lcn;
            this.clusters = clusters;
        }
    }
}
