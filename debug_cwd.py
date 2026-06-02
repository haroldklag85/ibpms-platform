import os
print("backend exists:", os.path.exists("backend"))
if os.path.exists("backend"):
    print("backend contents:", os.listdir("backend"))
    print("backend/ibpms-core exists:", os.path.exists("backend/ibpms-core"))
    if os.path.exists("backend/ibpms-core"):
        print("backend/ibpms-core contents:", os.listdir("backend/ibpms-core"))
        print("backend/ibpms-core/target exists:", os.path.exists("backend/ibpms-core/target"))
        if os.path.exists("backend/ibpms-core/target"):
            print("backend/ibpms-core/target contents:", os.listdir("backend/ibpms-core/target"))
