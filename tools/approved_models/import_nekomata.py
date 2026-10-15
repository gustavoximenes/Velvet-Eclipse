"""Export approved Nekomata 02 once, retaining the existing runtime asset ID."""
from pathlib import Path
import json
import export_models as common

def main():
 common.SCALES['nekomata']=.125
 common.IMPACTS.update(physical=.91,magic=1.10)
 report=common.export('nekomata')
 report.update(revision='02',authoring_density=8,impact_seconds={'physical':.91,'magic':1.10},runtime_impact_seconds=1.4,runtime_duration=2.5,axis_conversion_count=1)
 (Path(__file__).parent/'nekomata/integration.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf8')
 print('Exported approved Nekomata02: original ID, Box UV, atlas, idle and both attacks; impact at 1.4 s.')

if __name__=='__main__':main()
