// codec-wasm - Rust implementation of encryption/decryption algorithms
// Directly exporting functions for WebAssembly without wasm-bindgen

use std::alloc::{alloc as rust_alloc, dealloc as rust_dealloc, Layout};
use sm3::Digest; // 导入Digest trait
use base64::{engine::general_purpose, Engine as _};

// 添加wasm-bindgen宏
use wasm_bindgen::prelude::*;
use wasm_bindgen::JsValue;
use wasm_bindgen::JsCast;
use js_sys::{Reflect, Object};

// We need to add memory management functions for WASM
#[wasm_bindgen]
pub fn alloc(size: usize) -> *mut u8 {
    if size == 0 {
        return std::ptr::null_mut();
    }
    
    let layout = Layout::from_size_align(size, 1).unwrap();
    unsafe { rust_alloc(layout) }
}

#[wasm_bindgen]
pub fn dealloc(ptr: *mut u8, size: usize) {
    if ptr.is_null() || size == 0 {
        return;
    }
    
    let layout = Layout::from_size_align(size, 1).unwrap();
    unsafe { rust_dealloc(ptr, layout) }
}

// String utilities
fn string_from_ptr(ptr: *const u8, len: usize) -> String {
    unsafe {
        let slice = std::slice::from_raw_parts(ptr, len);
        String::from_utf8_lossy(slice).into_owned()
    }
}

fn string_to_ptr(s: &str) -> *mut u8 {
    let bytes = s.as_bytes();
    let len = bytes.len();
    
    // 分配内存，额外分配一个字节用于null终止符
    let ptr = alloc(len + 1);
    if ptr.is_null() {
        return ptr;
    }
    
    // 复制数据到分配的内存
    unsafe {
        std::ptr::copy_nonoverlapping(bytes.as_ptr(), ptr, len);
        // 添加null终止符
        std::ptr::write(ptr.add(len), 0);
    }
    
    ptr
}


// SM2 key pair generation function
#[wasm_bindgen]
pub fn generate_sm2_key_pair() -> *mut u8 {
    // 创建一个简单的JSON格式密钥对（占位符实现）
    let keypair_json = "{\"privateKey\":\"0123456789abcdef\",\"publicKey\":\"fedcba9876543210\"}";
    
    // 返回字符串指针
    string_to_ptr(keypair_json)
}

// SM2 encryption function
#[wasm_bindgen]
pub fn sm2_encrypt(data_ptr: *const u8, data_len: usize, public_key_ptr: *const u8, public_key_len: usize) -> *mut u8 {
    // 获取字符串数据
    let data = string_from_ptr(data_ptr, data_len);
    let _public_key = string_from_ptr(public_key_ptr, public_key_len);
    
    // 返回加密结果（占位符实现）
    let encrypted_data = format!("SM2_ENCRYPTED:{}", data);
    
    // 返回加密结果
    string_to_ptr(&encrypted_data)
}

// SM2 decryption function
#[wasm_bindgen]
pub fn sm2_decrypt(encrypted_data_ptr: *const u8, encrypted_data_len: usize, private_key_ptr: *const u8, private_key_len: usize) -> *mut u8 {
    // 获取字符串数据
    let encrypted_data = string_from_ptr(encrypted_data_ptr, encrypted_data_len);
    let _private_key = string_from_ptr(private_key_ptr, private_key_len);
    
    // 返回解密结果（占位符实现）
    let decrypted_data = encrypted_data.replace("SM2_ENCRYPTED:", "");
    
    // 返回解密结果
    string_to_ptr(&decrypted_data)
}

// uu2_process_request：从request.codecKey写入header并删除临时字段
#[wasm_bindgen]
pub fn uu2_process_request(request: JsValue) -> JsValue {
    if !request.is_object() {
        return request;
    }
    let req = request.clone();

    // 确保headers存在
    let mut headers = Reflect::get(&req, &JsValue::from_str("headers")).unwrap_or(JsValue::UNDEFINED);
    if headers.is_undefined() || headers.is_null() || !headers.is_object() {
        let obj = Object::new();
        let _ = Reflect::set(&req, &JsValue::from_str("headers"), &obj);
        headers = obj.into();
    }

    // 如果header中没有access-control-origin-key，则尝试从codecKey获取并写入，然后删除codecKey
    let has_key = Reflect::get(&headers, &JsValue::from_str("access-control-origin-key")).unwrap_or(JsValue::UNDEFINED);
    let mut need_set = has_key.is_undefined() || has_key.is_null() || has_key.as_string().map(|s| s.is_empty()).unwrap_or(true);

    if need_set {
        if let Ok(tmp) = Reflect::get(&req, &JsValue::from_str("codecKey")) {
            if let Some(s) = tmp.as_string() {
                if !s.is_empty() {
                    let _ = Reflect::set(&headers, &JsValue::from_str("access-control-origin-key"), &JsValue::from_str(&s));
                    let _ = js_sys::Reflect::delete_property(&req, &JsValue::from_str("codecKey"));
                    need_set = false;
                }
            }
        }
    }

    req
}
