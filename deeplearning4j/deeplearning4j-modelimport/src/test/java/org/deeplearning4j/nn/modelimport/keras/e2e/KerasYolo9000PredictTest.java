/*
 *  ******************************************************************************
 *  *
 *  *
 *  * This program and the accompanying materials are made available under the
 *  * terms of the Apache License, Version 2.0 which is available at
 *  * https://www.apache.org/licenses/LICENSE-2.0.
 *  *
 *  *  See the NOTICE file distributed with this work for additional
 *  *  information regarding copyright ownership.
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  * License for the specific language governing permissions and limitations
 *  * under the License.
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *  *****************************************************************************
 */
package org.deeplearning4j.nn.modelimport.keras.e2e;

import lombok.extern.slf4j.Slf4j;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.BaseDL4JTest;
import org.deeplearning4j.nn.modelimport.keras.KerasLayer;
import org.deeplearning4j.nn.modelimport.keras.KerasModelImport;
import org.deeplearning4j.nn.modelimport.keras.layers.convolutional.KerasSpaceToDepth;
import org.deeplearning4j.nn.transferlearning.TransferLearning;
import org.deeplearning4j.util.ModelSerializer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.nd4j.common.tests.tags.NativeTag;
import org.nd4j.common.tests.tags.TagNames;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.nd4j.linalg.factory.Nd4j;
import java.io.File;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;

@Slf4j
@DisplayName("Keras Yolo 9000 Predict Test")
@Tag(TagNames.FILE_IO)
@Tag(TagNames.KERAS)
@NativeTag
class KerasYolo9000PredictTest extends BaseDL4JTest {

    private static final String DL4J_MODEL_FILE_NAME = ".";

    private static ImagePreProcessingScaler IMAGE_PREPROCESSING_SCALER = new ImagePreProcessingScaler(0, 1);

    @Test
    @Disabled("Need to manually download file for ylo.")
    @DisplayName("Test Yolo Prediction Import")
    void testYoloPredictionImport() throws Exception {
        int HEIGHT = 416;
        int WIDTH = 416;
        INDArray indArray = Nd4j.create(HEIGHT, WIDTH, 3);
        IMAGE_PREPROCESSING_SCALER.transform(indArray);
        KerasLayer.registerCustomLayer("Lambda", KerasSpaceToDepth.class);
        String h5_FILENAME = "modelimport/keras/examples/yolo/yolo-voc.h5";
        ComputationGraph graph = KerasModelImport.importKerasModelAndWeights(h5_FILENAME, false);
        double[][] priorBoxes = { { 1.3221, 1.73145 }, { 3.19275, 4.00944 }, { 5.05587, 8.09892 }, { 9.47112, 4.84053 }, { 11.2364, 10.0071 } };
        INDArray priors = Nd4j.create(priorBoxes);
        ComputationGraph model = new TransferLearning.GraphBuilder(graph).addLayer("outputs", new org.deeplearning4j.nn.conf.layers.objdetect.Yolo2OutputLayer.Builder().boundingBoxPriors(priors).build(), "conv2d_23").setOutputs("outputs").build();
        ModelSerializer.writeModel(model, DL4J_MODEL_FILE_NAME, false);
        ComputationGraph computationGraph = ModelSerializer.restoreComputationGraph(new File(DL4J_MODEL_FILE_NAME));
        System.out.println(computationGraph.summary(InputType.convolutional(416, 416, 3)));
        INDArray results = computationGraph.outputSingle(indArray);
    }
}
