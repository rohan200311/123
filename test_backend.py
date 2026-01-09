import sys
import os
sys.path.append(os.getcwd())
from backend_service.app.modules import SuperResolutionModule
from PIL import Image

def test_sr():
    mod = SuperResolutionModule()
    img = Image.new('RGB', (100, 100))
    out = mod.apply(img)
    assert out.size == (200, 200)
    print("SR Module Test Passed")

if __name__ == "__main__":
    test_sr()
