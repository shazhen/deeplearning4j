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
package org.deeplearning4j.nn.modelimport.keras.layers.convolution;

import org.deeplearning4j.nn.conf.ConvolutionMode;
import org.deeplearning4j.nn.conf.dropout.Dropout;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.BaseDL4JTest;
import org.deeplearning4j.nn.modelimport.keras.KerasTestUtils;
import org.deeplearning4j.nn.modelimport.keras.config.Keras1LayerConfiguration;
import org.deeplearning4j.nn.modelimport.keras.config.Keras2LayerConfiguration;
import org.deeplearning4j.nn.modelimport.keras.config.KerasLayerConfiguration;
import org.deeplearning4j.nn.modelimport.keras.layers.convolutional.KerasConvolution3D;
import org.deeplearning4j.nn.weights.IWeightInit;
import org.deeplearning4j.nn.weights.WeightInitXavier;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.nd4j.common.tests.tags.NativeTag;
import org.nd4j.common.tests.tags.TagNames;

/**
 * @author Max Pumperla
 */
@DisplayName("Keras Convolution 3 D Test")
@Tag(TagNames.FILE_IO)
@Tag(TagNames.KERAS)
@NativeTag
class KerasConvolution3DTest extends BaseDL4JTest {

    private final String ACTIVATION_KERAS = "linear";

    private final String ACTIVATION_DL4J = "identity";

    private final String LAYER_NAME = "test_layer";

    private final String INIT_KERAS = "glorot_normal";

    private final IWeightInit INIT_DL4J = new WeightInitXavier();

    private final double L1_REGULARIZATION = 0.01;

    private final double L2_REGULARIZATION = 0.02;

    private final double DROPOUT_KERAS = 0.3;

    private final double DROPOUT_DL4J = 1 - DROPOUT_KERAS;

    private final int[] KERNEL_SIZE = new int[] { 1, 2, 3 };

    private final int[] STRIDE = new int[] { 3, 4, 5 };

    private final int N_OUT = 13;

    private final String BORDER_MODE_VALID = "valid";

    private final int[] VALID_PADDING = new int[] { 0, 0, 0 };

    private Integer keras1 = 1;

    private Integer keras2 = 2;

    private Keras1LayerConfiguration conf1 = new Keras1LayerConfiguration();

    private Keras2LayerConfiguration conf2 = new Keras2LayerConfiguration();

    @Test
    @DisplayName("Test Convolution 3 D Layer")
    void testConvolution3DLayer() throws Exception {
        buildConvolution3DLayer(conf1, keras1);
        buildConvolution3DLayer(conf2, keras2);
    }

    private void buildConvolution3DLayer(KerasLayerConfiguration conf, Integer kerasVersion) throws Exception {
        Map<String, Object> layerConfig = new HashMap<>();
        layerConfig.put(conf.getLAYER_FIELD_CLASS_NAME(), conf.getLAYER_CLASS_NAME_CONVOLUTION_3D());
        Map<String, Object> config = new HashMap<>();
        config.put(conf.getLAYER_FIELD_ACTIVATION(), ACTIVATION_KERAS);
        config.put(conf.getLAYER_FIELD_NAME(), LAYER_NAME);
        if (kerasVersion == 1) {
            config.put(conf.getLAYER_FIELD_INIT(), INIT_KERAS);
        } else {
            Map<String, Object> init = new HashMap<>();
            init.put("class_name", conf.getINIT_GLOROT_NORMAL());
            config.put(conf.getLAYER_FIELD_INIT(), init);
        }
        Map<String, Object> W_reg = new HashMap<>();
        W_reg.put(conf.getREGULARIZATION_TYPE_L1(), L1_REGULARIZATION);
        W_reg.put(conf.getREGULARIZATION_TYPE_L2(), L2_REGULARIZATION);
        config.put(conf.getLAYER_FIELD_W_REGULARIZER(), W_reg);
        config.put(conf.getLAYER_FIELD_DROPOUT(), DROPOUT_KERAS);
        if (kerasVersion == 1) {
            config.put(conf.getLAYER_FIELD_3D_KERNEL_1(), KERNEL_SIZE[0]);
            config.put(conf.getLAYER_FIELD_3D_KERNEL_2(), KERNEL_SIZE[1]);
            config.put(conf.getLAYER_FIELD_3D_KERNEL_3(), KERNEL_SIZE[2]);
        } else {
            ArrayList kernel = new ArrayList<Integer>() {

                {
                    for (int i : KERNEL_SIZE) add(i);
                }
            };
            config.put(conf.getLAYER_FIELD_KERNEL_SIZE(), kernel);
        }
        List<Integer> subsampleList = new ArrayList<>();
        subsampleList.add(STRIDE[0]);
        subsampleList.add(STRIDE[1]);
        subsampleList.add(STRIDE[2]);
        config.put(conf.getLAYER_FIELD_CONVOLUTION_STRIDES(), subsampleList);
        config.put(conf.getLAYER_FIELD_NB_FILTER(), N_OUT);
        config.put(conf.getLAYER_FIELD_BORDER_MODE(), BORDER_MODE_VALID);
        layerConfig.put(conf.getLAYER_FIELD_CONFIG(), config);
        layerConfig.put(conf.getLAYER_FIELD_KERAS_VERSION(), kerasVersion);
        ConvolutionLayer layer = new KerasConvolution3D(layerConfig).getConvolution3DLayer();
        assertEquals(ACTIVATION_DL4J, layer.getActivationFn().toString());
        assertEquals(LAYER_NAME, layer.getLayerName());
        assertEquals(INIT_DL4J, layer.getWeightInitFn());
        assertEquals(L1_REGULARIZATION, KerasTestUtils.getL1(layer), 0.0);
        assertEquals(L2_REGULARIZATION, KerasTestUtils.getL2(layer), 0.0);
        assertEquals(new Dropout(DROPOUT_DL4J), layer.getIDropout());
        assertArrayEquals(KERNEL_SIZE, layer.getKernelSize());
        assertArrayEquals(STRIDE, layer.getStride());
        assertEquals(N_OUT, layer.getNOut());
        assertEquals(ConvolutionMode.Truncate, layer.getConvolutionMode());
        assertArrayEquals(VALID_PADDING, layer.getPadding());
    }
}
