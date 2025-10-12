// codec-wasm JavaScript包装器

let wasmModule = null;
let wasmLoaded = false;

// 异步加载WASM模块
async function loadWasmAsync() {
  if (wasmLoaded) {
    return wasmModule;
  }

  try {
    // 直接加载WASM文件
    const wasmUrl = new URL('../build/codec_wasm.wasm', import.meta.url);
    const wasmBytes = await fetch(wasmUrl).then(response => response.arrayBuffer());
    const wasmModule = await WebAssembly.instantiate(wasmBytes);
    
    // 获取WASM实例的导出函数
    const { exports } = wasmModule.instance || wasmModule;
    
    // 保存WASM模块实例
    wasmModuleInstance = exports;
    wasmLoaded = true;
    
    console.log('Rust Codec WASM module loaded successfully');
    return exports;
  } catch (error) {
    console.error('Failed to load Rust WASM module:', error);
    throw new Error('Failed to load codec WASM module: ' + error.message);
  }
}

// 异步初始化函数（用于应用启动时加载）
export async function initializeWasmModule() {
  if (wasmLoaded) {
    return wasmModuleInstance;
  }

  await loadWasmAsync();
  return wasmModuleInstance;
}

// 检查WASM是否已加载
export function isWasmLoaded() {
  return wasmLoaded;
}

// WASM版本的uu2函数（同步方式）
// 修改函数签名: 接收PureHttpRequestConfig对象，而不是分别传入各个参数
export function uu2_wasm(request) {
  // 确保WASM已加载
  if (!wasmLoaded) {
    throw new Error('WASM module not loaded. Please call initializeWasmModule() first or wait for initialization.');
  }
  
  // 检查请求对象是否存在
  if (!request) {
    throw new Error('Request object is required');
  }
  
  // 从配置判断是否开启加密
  const configOpen = getConfig('requestCodecOpen') === true;
  const codecRequestKey = getConfig('codecRequestKey') || '';
  
  // 如果未开启加密，直接返回原始请求对象
  if (!configOpen || !codecRequestKey) {
    return request;
  }
  
  // 检查请求URL是否为SETTING_PATH
  const requestUrl = request.url || '';
  if (requestUrl.startsWith('/v2/setting')) {
    return request;
  }
  
  // 检查请求数据是否存在
  if (!request.data) {
    return request;
  }
  
  // 检查请求数据是否包含文件或Blob对象
  let hasFileOrBlob = false;
  if (request.data instanceof Array) {
    // 数组情况
    // 在Rust实现中，我们假设数组不包含文件或Blob对象
  } else if (typeof request.data === 'object') {
    // 对象情况
    const keys = Object.keys(request.data);
    for (const key of keys) {
      const val = request.data[key];
      if (val instanceof File || val instanceof Blob) {
        hasFileOrBlob = true;
        break;
      }
    }
  }
  
  // 如果包含文件或Blob对象，不进行加密
  if (hasFileOrBlob) {
    return request;
  }
  
  // 提取请求数据
  const requestData = typeof request.data === 'string' ? request.data : JSON.stringify(request.data);
  
  // 调用WASM函数处理加密数据
  try {
    // 注意：直接调用WASM导出的函数需要确保函数名和参数匹配
    const encryptedData = wasmModuleInstance.uu2(requestData, requestUrl, configOpen.toString(), codecRequestKey);
    
    // 将加密后的数据替换到request.data中
    try {
      request.data = JSON.parse(encryptedData);
    } catch (parseError) {
      // If parsing fails, use the encrypted data as is
      request.data = encryptedData;
    }
    
    // 添加时间戳到header中
    if (!request.headers) {
      request.headers = {};
    }
    request.headers["access-control-origin-key"] = Date.now();
    
    // 返回修改后的请求对象
    return request;
  } catch (error) {
    console.error('WASM encryption failed:', error);
    // 如果加密失败，返回原始请求
    return request;
  }
}

// 添加getConfig函数的实现
function getConfig(key) {
  // 在浏览器环境中，从全局配置获取
  if (typeof window !== 'undefined' && window.__APP_CONFIG__) {
    return window.__APP_CONFIG__[key];
  }
  // 如果没有全局配置，返回默认值或undefined
  return undefined;
}

// WASM版本的uu1函数（同步方式）
// 修改函数签名: 接收PureHttpResponse对象，而不是分别传入各个参数
export function uu1_wasm(response) {
  // 确保WASM已加载
  if (!wasmLoaded) {
    throw new Error('WASM module not loaded. Please call initializeWasmModule() first or wait for initialization.');
  }
  
  // 检查响应对象是否存在
  if (!response) {
    throw new Error('Response object is required');
  }
  
  // 检查响应状态
  const status = response.status || 0;
  
  // 检查响应头中是否包含originKey
  const headers = response.headers || {};
  const originKey = headers['access-control-origin-key'] || '';
  
  // 如果不包含originKey，直接返回原始响应
  if (!originKey) {
    return response;
  }
  
  // 包含originKey，说明是加密数据，需要处理
  const timestamp = headers['access-control-timestamp-user'] || '';
  
  // 从response.data中提取需要解密的数据
  let dataToDecrypt = '';
  if (response.data) {
    if (typeof response.data === 'object' && response.data.data) {
      dataToDecrypt = response.data.data;
    } else {
      dataToDecrypt = response.data;
    }
  }
  
  // 调用WASM函数处理加密数据
  try {
    // 注意：直接调用WASM导出的函数需要确保函数名和参数匹配
    const decryptedData = wasmModuleInstance.uu1(status.toString(), typeof dataToDecrypt === 'string' ? dataToDecrypt : JSON.stringify(dataToDecrypt), originKey, timestamp);
    
    // 尝试解析解密后的数据
    try {
      response.data = JSON.parse(decryptedData);
    } catch (parseError) {
      // If parsing fails, use the decrypted data as is
      response.data = decryptedData;
    }
    
    // 删除相关的header数据
    delete headers['access-control-origin-key'];
    delete headers['access-control-timestamp-user'];
    delete headers['access-control-nonce'];
    
    // 返回修改后的响应对象
    return response;
  } catch (error) {
    console.error('WASM decryption failed:', error);
    // 如果解密失败，返回原始响应
    return response;
  }
}

// WASM版本的uu3函数（同步方式）
export function uu3_wasm(value) {
  // 确保WASM已加载
  if (!wasmLoaded) {
    throw new Error('WASM module not loaded. Please call initializeWasmModule() first or wait for initialization.');
  }
  
  // 直接调用WASM函数
  try {
    return wasmModuleInstance.uu3(value);
  } catch (error) {
    console.error('WASM uu3 failed:', error);
    return value;
  }
}

// WASM版本的uu4函数（同步方式）
export function uu4_wasm(response) {
  // 确保WASM已加载
  if (!wasmLoaded) {
    throw new Error('WASM module not loaded. Please call initializeWasmModule() first or wait for initialization.');
  }
  
  // 检查响应对象是否存在
  if (!response) {
    return {};
  }
  
  // 提取数据
  const responseData = response.data || '';
  const uuid = response.uuid || '';
  const timestamp = response.timestamp || '';
  
  // 直接调用WASM函数
  try {
    const result = wasmModuleInstance.uu4(responseData, uuid, timestamp);
    
    // 尝试解析结果
    try {
      return JSON.parse(result);
    } catch (parseError) {
      // If parsing fails, return an empty object
      return {};
    }
  } catch (error) {
    console.error('WASM uu4 failed:', error);
    return {};
  }
}

// 导出WASM模块的其他函数（同步方式）
export function getCurrentTimestamp() {
  // 确保WASM已加载
  if (!wasmLoaded) {
    throw new Error('WASM module not loaded. Please call initializeWasmModule() first or wait for initialization.');
  }
  return wasmModuleInstance.get_current_timestamp();
}

export function add(a, b) {
  // 确保WASM已加载
  if (!wasmLoaded) {
    throw new Error('WASM module not loaded. Please call initializeWasmModule() first or wait for initialization.');
  }
  return wasmModuleInstance.add(a, b);
}

// 导出generateNonce函数（同步方式）
export function generateNonce() {
  // 确保WASM已加载
  if (!wasmLoaded) {
    throw new Error('WASM module not loaded. Please call initializeWasmModule() first or wait for initialization.');
  }
  return wasmModuleInstance.generate_nonce();
}

// 导出MD5哈希函数（同步方式）
export function md5Hash(input) {
  // 确保WASM已加载
  if (!wasmLoaded) {
    throw new Error('WASM module not loaded. Please call initializeWasmModule() first or wait for initialization.');
  }
  return wasmModuleInstance.md5_hash(input);
}

// 导出generateSign函数（同步方式）
export function generateSign(paramsJson, timestamp, nonce, secretKey) {
  // 确保WASM已加载
  if (!wasmLoaded) {
    throw new Error('WASM module not loaded. Please call initializeWasmModule() first or wait for initialization.');
  }
  return wasmModuleInstance.generate_sign(paramsJson, timestamp, nonce, secretKey);
}

// 导出processRequest函数（同步方式）
export function processRequest(requestData, requestUrl, codecConfig, codecKey) {
  // 确保WASM已加载
  if (!wasmLoaded) {
    throw new Error('WASM module not loaded. Please call initializeWasmModule() first or wait for initialization.');
  }
  return wasmModuleInstance.process_request(requestData, requestUrl, codecConfig, codecKey);
}

// 导出processResponse函数（同步方式）
export function processResponse(responseData, originKey, timestamp) {
  // 确保WASM已加载
  if (!wasmLoaded) {
    throw new Error('WASM module not loaded. Please call initializeWasmModule() first or wait for initialization.');
  }
  return wasmModuleInstance.process_response(responseData, originKey, timestamp);
}

// 导出AES加密函数（同步方式）
export function encryptAES(data, key) {
  // 确保WASM已加载
  if (!wasmLoaded) {
    throw new Error('WASM module not loaded. Please call initializeWasmModule() first or wait for initialization.');
  }
  return wasmModuleInstance.aes_encrypt(data, key);
}

// 导出AES解密函数（同步方式）
export function decryptAES(value, key) {
  // 确保WASM已加载
  if (!wasmLoaded) {
    throw new Error('WASM module not loaded. Please call initializeWasmModule() first or wait for initialization.');
  }
  return wasmModuleInstance.aes_decrypt(value, key);
}

// Storage Key加密函数（同步方式）
export function encryptStorageKey(key, systemCode) {
  // 确保WASM已加载
  if (!wasmLoaded) {
    throw new Error('WASM module not loaded. Please call initializeWasmModule() first or wait for initialization.');
  }
  return wasmModuleInstance.encrypt_storage_key(key, systemCode);
}

// Storage Value加密函数（同步方式）
export function encryptStorageValue(value, key, systemCode, storageKey, storageEncode) {
  // 确保WASM已加载
  if (!wasmLoaded) {
    throw new Error('WASM module not loaded. Please call initializeWasmModule() first or wait for initialization.');
  }
  
  // 调用WASM函数处理加密数据
  try {
    return wasmModuleInstance.encrypt_storage_value(value, key, systemCode, storageKey, storageEncode);
  } catch (error) {
    console.error('WASM encryptStorageValue failed:', error);
    // 如果加密失败，返回原始value
    return value;
  }
}

// Storage Value解密函数（同步方式）
export function decryptStorageValue(value, key, systemCode, storageKey, storageEncode) {
  // 确保WASM已加载
  if (!wasmLoaded) {
    throw new Error('WASM module not loaded. Please call initializeWasmModule() first or wait for initialization.');
  }
  
  // 调用WASM函数处理解密数据
  try {
    return wasmModuleInstance.decrypt_storage_value(value, key, systemCode, storageKey, storageEncode);
  } catch (error) {
    console.error('WASM decryptStorageValue failed:', error);
    // 如果解密失败，返回原始value
    return value;
  }
}

// 挂载WASM模块到window对象上，方便全局调用
if (typeof window !== 'undefined') {
  window.codecWasm = {
    initializeWasmModule,
    isWasmLoaded,
    uu2_wasm,
    uu1_wasm,
    uu3_wasm,
    uu4_wasm,
    getCurrentTimestamp,
    add,
    generateNonce,
    md5Hash,
    generateSign,
    processRequest,
    processResponse,
    encryptAES,
    decryptAES,
    encryptStorageKey,
    encryptStorageValue,
    decryptStorageValue
  };
}

export default wasmModuleInstance;