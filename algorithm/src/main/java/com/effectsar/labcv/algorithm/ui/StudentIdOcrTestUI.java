//package com.effectsar.labcv.algorithm.ui.ui;
//
//import com.effectsar.labcv.demo.R;
//import com.effectsar.labcv.demo.core.v4.algorithm.AlgorithmInterface;
//import com.effectsar.labcv.demo.core.v4.algorithm.task.StudentIdOcrTask;
//import com.effectsar.labcv.demo.model.AlgorithmItem;
//import com.effectsar.labcv.effectsdk.BefStudentIdOcrInfo;
//import com.effectsar.labcv.effectsdk.library.LogUtils;
//
///**
// * Created on 2020/9/18 16:58
// */
//public class StudentIdOcrTestUI extends BaseAlgorithmUI {
//
//    @Override
//    void initCallback() {
//        super.initCallback();
//
//        provider().getAlgorithm().addResultCallback(new AlgorithmInterface.ResultCallback<BefStudentIdOcrInfo>() {
//            @Override
//            protected void doResult(BefStudentIdOcrInfo befStudentIdOcrInfo, int framecount) {
//                LogUtils.e("student ocr info: " + befStudentIdOcrInfo.toString());
//            }
//        });
//    }
//
//    @Override
//    public AlgorithmItem getAlgorithmItem() {
//        AlgorithmItem item = new AlgorithmItem(StudentIdOcrTask.STUDENT_ID_OCR);
//        item.setIcon(R.drawable.ic_skeleton);
//        item.setTitle(R.string.tab_student_id_ocr_test);
//        item.setTabTitleId(R.string.tab_student_id_ocr_test);
//        return item;
//    }
//}
