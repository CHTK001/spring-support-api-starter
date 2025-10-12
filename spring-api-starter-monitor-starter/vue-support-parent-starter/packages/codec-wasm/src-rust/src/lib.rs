// codec-wasm - Rust implementation of encryption/decryption algorithms
// Directly exporting functions for WebAssembly without wasm-bindgen

use std::alloc::{alloc as rust_alloc, dealloc as rust_dealloc, Layout};
use sm3::Digest; // 导入Digest trait
use base64::{engine::general_purpose, Engine as _};

// 添加wasm-bindgen宏
use wasm_bindgen::prelude::*;

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