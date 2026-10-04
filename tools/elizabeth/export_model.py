"""Export the approved Elizabeth 05; the older study stays archived here."""
from pathlib import Path
import runpy
api=runpy.run_path(str(Path(__file__).resolve().parents[1]/'approved_models/export_models.py'))
api['export']('elizabeth')
print('Elizabeth 05 exported from tools/approved_models/elizabeth.')
