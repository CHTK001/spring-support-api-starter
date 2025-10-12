#!/usr/bin/env node

import { execSync } from 'child_process';
import { existsSync, mkdirSync, copyFileSync } from 'fs';
import { join, resolve } from 'path';

// Get the project root directory
const projectRoot = resolve(process.cwd());

// Define paths
const rustSrcDir = join(projectRoot, 'src-rust');
const buildDir = join(projectRoot, 'build');

console.log('Building Rust WASM module...');

try {
  // Check if Rust and wasm-pack are installed
  try {
    execSync('rustc --version', { stdio: 'pipe' });
    console.log('✓ Rust is installed');
  } catch (error) {
    console.error('✗ Rust is not installed. Please install Rust from https://www.rust-lang.org/');
    process.exit(1);
  }

  // Add wasm32-unknown-unknown target if not already added
  try {
    execSync('rustup target add wasm32-unknown-unknown', { stdio: 'pipe' });
    console.log('✓ wasm32-unknown-unknown target added');
  } catch (error) {
    console.log('✓ wasm32-unknown-unknown target already installed');
  }

  // Create build directory if it doesn't exist
  if (!existsSync(buildDir)) {
    mkdirSync(buildDir, { recursive: true });
  }

  // Build the Rust WASM module directly to WASM without JS bindings
  console.log('Compiling Rust code to WASM...');
  execSync(`cd ${rustSrcDir} && cargo build --target wasm32-unknown-unknown --release`, { 
    stdio: 'inherit'
  });

  // Copy the generated WASM file to the build directory
  const wasmSource = join(rustSrcDir, 'target', 'wasm32-unknown-unknown', 'release', 'codec_wasm.wasm');
  const wasmDest = join(buildDir, 'codec_wasm.wasm');
  copyFileSync(wasmSource, wasmDest);

  console.log('✓ Rust WASM build completed successfully!');
  console.log(`✓ WASM file is located at ${wasmDest}`);
  
} catch (error) {
  console.error('✗ Error building Rust WASM module:', error.message);
  process.exit(1);
}