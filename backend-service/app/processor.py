import os
from PIL import Image, ImageEnhance, ImageFilter, ImageOps
import json
from .modules import SuperResolutionModule, CameraLookModule, AutoEnhanceModule

class ImageProcessor:
    def __init__(self):
        self.super_res = SuperResolutionModule()
        self.camera_look = CameraLookModule()
        self.auto_enhance = AutoEnhanceModule()

    def process(self, input_path: str, options: dict, output_dir: str):
        """
        Main pipeline execution.
        """
        # Load image and preserve EXIF if possible
        original_image = Image.open(input_path)
        exif_data = original_image.info.get("exif")
        image = original_image.convert("RGB")

        analysis_report = {}

        modules = options.get("modules", [])
        params = options.get("parameters", {})

        # Module 4: Auto Enhance (Analyze first)
        if "auto_enhance" in modules:
            image, report = self.auto_enhance.apply(image)
            analysis_report = report

        # Module 1: Super Resolution
        if "super_res" in modules:
            image = self.super_res.apply(image)

        # Module 2: Camera Look Emulation
        if "color_profile" in modules:
            profile_name = params.get("profile_name", "standard")
            image = self.camera_look.apply(image, profile_name)

        # Module 9: Noise Reduction
        if "noise_reduction" in modules:
             # Simple simulated AI Denoise
             image = image.filter(ImageFilter.GaussianBlur(1))

        # Module 3: Image Quality Engine (Mocked basic adjustments)
        if "hdr" in modules:
             enhancer = ImageEnhance.Contrast(image)
             image = enhancer.enhance(1.2) # Mock HDR

        # Save Result
        output_format = params.get("output_format", "JPEG").upper()
        save_params = {}

        if output_format == "JPEG":
            ext = ".jpg"
            save_params["quality"] = params.get("quality", 90)
            # Include EXIF if requested
            if params.get("include_exif", True) and exif_data:
                save_params["exif"] = exif_data
        else:
            ext = ".png"
            if params.get("include_exif", True) and exif_data:
                save_params["exif"] = exif_data

        filename = os.path.basename(input_path) + "_processed" + ext
        output_path = os.path.join(output_dir, filename)

        image.save(output_path, format=output_format, **save_params)

        return output_path, analysis_report
