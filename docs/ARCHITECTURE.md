# Google Jules Camera System Architecture

## Overview
This system implements a hybrid cloud-edge computational photography engine for Android. It leverages on-device capabilities for real-time preview and lightweight capture, while offloading heavy AI processing (Super Resolution, Semantic Enhancement) to a "Gemini Nano Banana Pro" backend.

## System Components

### 1. Android Client (`android-client`)
- **CameraX Integration**: Handles raw capture and preview stream.
- **Local Pre-processing**: resizing for preview, basic JPEG compression.
- **Network Layer**: Secure gRPC/REST client to upload images and metadata.
- **Editor UI**: State management (Undo/Redo), crop tool, and profile selection.

### 2. Backend Service (`backend-service`)
- **API Gateway**: Authenticates users and routes requests.
- **Processing Pipeline**: DAG (Directed Acyclic Graph) based image processing.
    - **Module 1**: Super Resolution Engine (Gemini Nano upscaling).
    - **Module 2**: Color Science / Look Emulation (LUTs/Curves).
    - **Module 3**: Image Quality Engine (HDR, Detail).
    - **Module 4**: Auto Enhance (Analysis + Correction).
- **Storage**: Temporary secure storage for processing artifacts.

## Data Flow
1.  **Capture**: User captures image (RAW/JPEG) on Android.
2.  **Upload**: Image is encrypted and uploaded to the backend with a job configuration (selected profiles, operations).
3.  **Process**:
    -   Backend decrypts and loads image.
    -   Pipeline executes requested modules (e.g., Upscale -> Denoise -> Color Grade).
    -   Result is encoded (JPEG/PNG).
4.  **Download**: Android client downloads the result.
5.  **Edit**: User can apply further edits (crop, new profile), creating a new job or applying local transforms if possible.

## Security
- **Encryption**: TLS 1.3 for transport. AES-256 for data at rest (temp).
- **Privacy**: Images are deleted from backend immediately after processing/download.

## Modules

- **Super Resolution**: Deep learning based.
- **Profiles**: Mathematical transformations on pixel data.
- **Undo/Redo**: Client-side history stack storing parameters; Server is stateless (mostly), reprocessing based on parameters.
