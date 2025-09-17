package com.effectsar.labcv.algorithm.ui;

import com.effectsar.labcv.core.algorithm.AvaBoostAlgorithmTask;
import com.effectsar.labcv.core.algorithm.BachSkeletonAlgorithmTask;
import com.effectsar.labcv.core.algorithm.C1AlgorithmTask;
import com.effectsar.labcv.core.algorithm.C2AlgorithmTask;
import com.effectsar.labcv.core.algorithm.CarAlgorithmTask;
import com.effectsar.labcv.core.algorithm.ChromaKeyingAlgorithmTask;
import com.effectsar.labcv.core.algorithm.ConcentrateAlgorithmTask;
import com.effectsar.labcv.core.algorithm.DynamicGestureAlgorithmTask;
import com.effectsar.labcv.core.algorithm.FaceAlgorithmTask;
import com.effectsar.labcv.core.algorithm.FaceClusterAlgorithmTask;
import com.effectsar.labcv.core.algorithm.FaceFittingAlgorithmTask;
import com.effectsar.labcv.core.algorithm.FaceVerifyAlgorithmTask;
import com.effectsar.labcv.core.algorithm.GazeEstimationAlgorithmTask;
import com.effectsar.labcv.core.algorithm.HairParserAlgorithmTask;
import com.effectsar.labcv.core.algorithm.HandAlgorithmTask;
import com.effectsar.labcv.core.algorithm.HeadSegAlgorithmTask;
import com.effectsar.labcv.core.algorithm.HumanDistanceAlgorithmTask;
import com.effectsar.labcv.core.algorithm.LicenseCakeAlgorithmTask;
import com.effectsar.labcv.core.algorithm.LightClsAlgorithmTask;
import com.effectsar.labcv.core.algorithm.PetFaceAlgorithmTask;
import com.effectsar.labcv.core.algorithm.PortraitMattingAlgorithmTask;
import com.effectsar.labcv.core.algorithm.SaliencyMattingAlgorithmTask;
import com.effectsar.labcv.core.algorithm.Skeleton3DAlgorithmTask;
import com.effectsar.labcv.core.algorithm.SkeletonAlgorithmTask;
import com.effectsar.labcv.core.algorithm.SkinSegmentationAlgorithmTask;
import com.effectsar.labcv.core.algorithm.SkySegAlgorithmTask;
import com.effectsar.labcv.core.algorithm.SlamAlgorithmTask;
import com.effectsar.labcv.core.algorithm.ObjectTrackingAlgorithmTask;
import com.effectsar.labcv.core.algorithm.StudentIdOcrAlgorithmTask;
import com.effectsar.labcv.core.algorithm.VideoClsAlgorithmTask;
import com.effectsar.labcv.core.algorithm.base.AlgorithmTaskKey;

import java.util.HashMap;
import java.util.Map;

public class AlgorithmUIFactory {
    private static final Map<AlgorithmTaskKey, AlgorithmUIGenerator> sMap = new HashMap<>();

    static {
        AlgorithmUIFactory.register(FaceAlgorithmTask.FACE, new AlgorithmUIFactory.AlgorithmUIGenerator() {
            @Override
            public AlgorithmUI create() {
                return new FaceUI();
            }
        });
        register(HeadSegAlgorithmTask.HEAD_SEGMENT, new AlgorithmUIGenerator() {
            @Override
            public AlgorithmUI create() {
                return new HeadSegUI();
            }
        });
        register(HairParserAlgorithmTask.HAIR_PARSER, new AlgorithmUIGenerator() {
            @Override
            public AlgorithmUI create() {
                return new HairParserUI();
            }
        });
        register(FaceVerifyAlgorithmTask.FACE_VERIFY, new AlgorithmUIGenerator() {
            @Override
            public AlgorithmUI create() {
                return new FaceVerifyUI();
            }
        });
        register(C1AlgorithmTask.C1, new AlgorithmUIGenerator() {
            @Override
            public AlgorithmUI create() {
                return new C1UI();
            }
        });
        register(C2AlgorithmTask.C2, new AlgorithmUIGenerator() {
            @Override
            public AlgorithmUI create() {
                return new C2UI();
            }
        });
        register(CarAlgorithmTask.CAR_ALGO, new AlgorithmUIGenerator() {
            @Override
            public AlgorithmUI create() {
                return new CarUI();
            }
        });
        register(ConcentrateAlgorithmTask.CONCENTRATION, new AlgorithmUIGenerator() {
            @Override
            public AlgorithmUI create() {
                return new ConcentrationUI();
            }
        });
        register(FaceClusterAlgorithmTask.FACE_CLUSTER, new AlgorithmUIGenerator() {
            @Override
            public AlgorithmUI create() {
                return new FaceClusterUI();
            }
        });
        register(GazeEstimationAlgorithmTask.GAZE_ESTIMATION, new AlgorithmUIGenerator() {
            @Override
            public AlgorithmUI create() {
                return new GazeEstimationUI();
            }
        });
        register(HairParserAlgorithmTask.HAIR_PARSER, new AlgorithmUIGenerator() {
            @Override
            public AlgorithmUI create() {
                return new HairParserUI();
            }
        });
        register(HandAlgorithmTask.HAND, new AlgorithmUIGenerator() {
            @Override
            public AlgorithmUI create() {
                return new HandUI();
            }
        });
        register(HumanDistanceAlgorithmTask.HUMAN_DISTANCE, new AlgorithmUIGenerator() {
            @Override
            public AlgorithmUI create() {
                return new HumanDistanceUI();
            }
        });
        register(LightClsAlgorithmTask.LIGHT_CLS, new AlgorithmUIGenerator() {
            @Override
            public AlgorithmUI create() {
                return new LightClsUI();
            }
        });
        register(PetFaceAlgorithmTask.PET_FACE, new AlgorithmUIGenerator() {
            @Override
            public AlgorithmUI create() {
                return new PetFaceUI();
            }
        });
        register(PortraitMattingAlgorithmTask.PORTRAIT_MATTING, new AlgorithmUIGenerator() {
            @Override
            public AlgorithmUI create() {
                return new PortraitMattingUI();
            }
        });
        register(SkeletonAlgorithmTask.SKELETON, new AlgorithmUIGenerator() {
            @Override
            public AlgorithmUI create() {
                return new SkeletonUI();
            }
        });
        register(SkySegAlgorithmTask.SKY_SEGMENT, new AlgorithmUIGenerator() {
            @Override
            public AlgorithmUI create() {
                return new SkySegUI();
            }
        });
        register(StudentIdOcrAlgorithmTask.STUDENT_ID_OCR, new AlgorithmUIGenerator() {
            @Override
            public AlgorithmUI create() {
                return new StudentIdOcrUI();
            }
        });
        register(VideoClsAlgorithmTask.VIDEO_CLS, new AlgorithmUIGenerator() {
            @Override
            public AlgorithmUI create() {
                return new VideoClsUI();
            }
        });
        register(DynamicGestureAlgorithmTask.DYNAMIC_GESTURE, new AlgorithmUIGenerator() {
            @Override
            public AlgorithmUI create() { return new DynamicGestureUI(); }
        });
        register(LicenseCakeAlgorithmTask.LICENSE_CAKE, new AlgorithmUIGenerator() {
            @Override
            public AlgorithmUI create() { return new LicenseCakeUI(); }
        });
        register(SkinSegmentationAlgorithmTask.SKIN_SEGMENTATION, new AlgorithmUIGenerator() {
            @Override
            public AlgorithmUI create() { return new SkinSegmentationUI(); }
        });
        register(BachSkeletonAlgorithmTask.BACH_SKELETON, new AlgorithmUIGenerator() {
            @Override
            public AlgorithmUI create() { return new BachSkeletonUI(); }
        });
        register(ChromaKeyingAlgorithmTask.CHROMA_KEYING, new AlgorithmUIGenerator() {
            @Override
            public AlgorithmUI create() { return new ChromaKeyingUI(); }
        });
        register(SlamAlgorithmTask.SLAM, new AlgorithmUIGenerator() {
            @Override
            public AlgorithmUI create() { return new SlamUI(); }
        });
        register(FaceFittingAlgorithmTask.FACE_FITTING, new AlgorithmUIGenerator() {
            @Override
            public AlgorithmUI create() {
                return new FaceFittingUI();
            }
        });
        register(Skeleton3DAlgorithmTask.SKELETON3D, new AlgorithmUIGenerator() {
            @Override
            public AlgorithmUI create() {
                return new Skeleton3DUI();
            }
        });
        register(AvaBoostAlgorithmTask.AVABOOST, new AlgorithmUIGenerator() {
            @Override
            public AlgorithmUI create() {
                return new AvaBoostUI();
            }
        });
        register(ObjectTrackingAlgorithmTask.OBJECT_TRACKING, new AlgorithmUIGenerator() {
            @Override
            public AlgorithmUI create() {
                return new ObjectTrackingUI();
            }
        });
        register(SaliencyMattingAlgorithmTask.SALIENCY_MATTING, new AlgorithmUIGenerator() {
            @Override
            public AlgorithmUI create() {
                return new SaliencyMattingUI();
            }
        });
    }

    public static void register(AlgorithmTaskKey key, AlgorithmUIGenerator generator) {
        sMap.put(key, generator);
    }

    public static AlgorithmUI create(AlgorithmTaskKey key) {
        AlgorithmUIGenerator generator = sMap.get(key);
        if (generator == null) {
            return null;
        }
        return generator.create();
    }

    public interface AlgorithmUIGenerator {
        AlgorithmUI create();
    }
}
