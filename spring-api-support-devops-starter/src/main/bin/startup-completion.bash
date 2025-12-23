#!/bin/bash
# ============================================================
# startup.sh Bash 自动补全脚本
# 
# 安装方式:
#   1. 复制到 /etc/bash_completion.d/startup-completion
#   2. 或在 ~/.bashrc 中添加: source /path/to/startup-completion.bash
#   3. 或执行: ./startup.sh completion install
# ============================================================

_startup_completion() {
    local cur prev words cword
    _init_completion || return

    # 主命令
    local main_commands="start stop restart status"
    
    # 子命令
    local sub_commands="info logs java arthas skywalking backup restore upgrade service dump gc top jvm docker"
    
    # 启动选项
    local start_opts="-f -j --skywalking --dev --prod --test --profile --debug --debug-port --jvm -h --help"
    
    # 子命令选项
    local info_opts="-a -c -m -f -h --help"
    local logs_opts="-f -t --filter --startup --app -h --help"
    local java_opts="-i -l -u -d --version -h --help"
    local arthas_opts="-i -u -v -h --help"
    local skywalking_opts="-i upgrade -u config --version -h --help"
    local backup_opts="-l --rm --clean -h --help"
    local restore_opts="-l -i --latest --clean -h --help"
    local upgrade_opts="-p --ignore --no-restart -h --help"
    local service_opts="-i --name -u enable disable status -h --help"
    local dump_opts="heap thread all -o --output -h --help"
    local gc_opts="full young -h --help"
    local top_opts="cpu mem -h --help"
    local jvm_opts="info flags tuning -h --help"
    local docker_opts="build deploy start stop restart status logs clean --name --version --port --host-port --tag --registry --lines -h --help"

    # 获取当前补全上下文
    case "${cword}" in
        1)
            # 第一个参数：主命令或子命令
            COMPREPLY=($(compgen -W "${main_commands} ${sub_commands} -h --help" -- "${cur}"))
            ;;
        *)
            # 根据第一个参数决定补全内容
            case "${words[1]}" in
                start)
                    COMPREPLY=($(compgen -W "${start_opts}" -- "${cur}"))
                    ;;
                stop|restart|status)
                    COMPREPLY=($(compgen -W "-h --help" -- "${cur}"))
                    ;;
                info)
                    COMPREPLY=($(compgen -W "${info_opts}" -- "${cur}"))
                    ;;
                logs)
                    COMPREPLY=($(compgen -W "${logs_opts}" -- "${cur}"))
                    ;;
                java)
                    COMPREPLY=($(compgen -W "${java_opts}" -- "${cur}"))
                    ;;
                arthas)
                    COMPREPLY=($(compgen -W "${arthas_opts}" -- "${cur}"))
                    ;;
                skywalking)
                    COMPREPLY=($(compgen -W "${skywalking_opts}" -- "${cur}"))
                    ;;
                backup)
                    COMPREPLY=($(compgen -W "${backup_opts}" -- "${cur}"))
                    ;;
                restore)
                    COMPREPLY=($(compgen -W "${restore_opts}" -- "${cur}"))
                    ;;
                upgrade)
                    COMPREPLY=($(compgen -W "${upgrade_opts}" -- "${cur}"))
                    ;;
                service)
                    COMPREPLY=($(compgen -W "${service_opts}" -- "${cur}"))
                    ;;
                dump)
                    COMPREPLY=($(compgen -W "${dump_opts}" -- "${cur}"))
                    ;;
                gc)
                    COMPREPLY=($(compgen -W "${gc_opts}" -- "${cur}"))
                    ;;
                top)
                    COMPREPLY=($(compgen -W "${top_opts}" -- "${cur}"))
                    ;;
                jvm)
                    COMPREPLY=($(compgen -W "${jvm_opts}" -- "${cur}"))
                    ;;
                docker)
                    COMPREPLY=($(compgen -W "${docker_opts}" -- "${cur}"))
                    ;;
                *)
                    # 处理带参数的选项
                    case "${prev}" in
                        -j|--profile|--debug-port|--jvm)
                            # 这些选项需要用户输入值
                            COMPREPLY=()
                            ;;
                        -t|--filter)
                            COMPREPLY=()
                            ;;
                        --version|-i|-p|--name|--rm|-o|--output)
                            COMPREPLY=()
                            ;;
                        --port|--host-port|--tag|--registry|--lines)
                            COMPREPLY=()
                            ;;
                        *)
                            COMPREPLY=($(compgen -W "${start_opts}" -- "${cur}"))
                            ;;
                    esac
                    ;;
            esac
            ;;
    esac

    return 0
}

# 注册补全函数
# 支持 startup.sh 和任何以 startup 开头的脚本名
complete -F _startup_completion startup.sh
complete -F _startup_completion ./startup.sh
complete -F _startup_completion startup

# 如果是 source 方式加载，输出提示
if [[ "${BASH_SOURCE[0]}" != "${0}" ]]; then
    echo "startup.sh 自动补全已加载"
fi
