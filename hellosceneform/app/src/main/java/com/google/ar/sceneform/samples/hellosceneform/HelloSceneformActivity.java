/*
 * Copyright 2018 Google LLC. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.ar.sceneform.samples.hellosceneform;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;
import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.ux.TransformableNode;

/**
 * This is an example activity that uses the Sceneform UX package to make common AR tasks easier.
 */
public class HelloSceneformActivity extends AppCompatActivity {
  private static final String TAG = HelloSceneformActivity.class.getSimpleName();
  private static final double MIN_OPENGL_VERSION = 3.0;

  private int[] grid = {
            1,0,0,0,0, 0,0,0,0,1,
            1,0,0,0,0, 0,0,0,0,1,
            1,0,0,0,0, 0,0,0,0,1,
            1,0,0,2,0, 0,2,0,0,1,
            1,0,0,0,0, 0,0,0,0,1,
            1,0,0,0,0, 0,0,0,0,1,
            1,0,0,0,0, 0,0,0,0,1,
            1,0,0,0,0, 0,0,0,0,1,
            1,0,0,0,0, 0,0,0,0,1,
            1,1,1,1,1, 1,1,1,1,1
  };
  private final int COL = 10;
  private final float SIZE_SCALE = 0.1f;
  private final float CENTER_SCALE = 0.1f;
  private ArFragment arFragment;
  private ModelRenderable[] shapeRenderables = new ModelRenderable[grid.length];
  private ModelRenderable planeRenderable;

  @Override
  @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
  // CompletableFuture requires api level 24
  // FutureReturnValueIgnored is not valid
  protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      if (!checkIsSupportedDeviceOrFinish(this)) {
          return;
      }
      setContentView(R.layout.activity_ux);
      arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);

      MaterialFactory.makeOpaqueWithColor(this, new Color(android.graphics.Color.BLACK)).thenAccept(
              material -> {
                  planeRenderable = ShapeFactory.makeCube(
                          new Vector3(1.0f,0.0f,1.0f),
                          new Vector3(0.0f,0.15f, 0.0f),
                          material);
              });

      for (int i = 0; i < grid.length; i++) {
          if (grid[i] != 0) {
              int x = (i % COL);
              int z = (int) -Math.floor(i / COL);

              int finalI = i;

              if (grid[i] == 1) {
                  MaterialFactory.makeOpaqueWithColor(this, new Color(android.graphics.Color.BLUE)).thenAccept(
                          material -> {
                              shapeRenderables[finalI] = ShapeFactory.makeCube(
                                      new Vector3(0.1f,0.3f,0.1f),
                                      new Vector3((float) x * CENTER_SCALE, 0.15f, (float) z * CENTER_SCALE),
                                      material);
                          });
              } else if (grid[i] == 2) {
                  MaterialFactory.makeOpaqueWithColor(this, new Color(android.graphics.Color.RED)).thenAccept(
                          material -> {
                              shapeRenderables[finalI] = ShapeFactory.makeSphere(
                                      0.1f,
                                      new Vector3((float) x * CENTER_SCALE, 0.15f, (float) z * CENTER_SCALE),
                                      material);
                          });
              }
          }
      }
      arFragment.setOnTapArPlaneListener(
        (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
          if (shapeRenderables == null) {
            return;
          }

          // Create the Anchor.
          Anchor anchor = hitResult.createAnchor();
          AnchorNode anchorNode = new AnchorNode(anchor);
          anchorNode.setParent(arFragment.getArSceneView().getScene());

          TransformableNode planeNode = new TransformableNode((arFragment.getTransformationSystem()));
          planeNode.setParent(anchorNode);
          planeNode.setRenderable(planeRenderable);
          planeNode.select();

          // Create the transformable andy and add it to the anchor.
            for (int i = 0; i < grid.length; i++) {
                if (grid[i] != 0) {
                    TransformableNode andy = new TransformableNode(arFragment.getTransformationSystem());
                    andy.setParent(planeNode);
                    andy.setRenderable(shapeRenderables[i]);
                    andy.select();
                }
            }
        });
}

  /**
   * Returns false and displays an error message if Sceneform can not run, true if Sceneform can run
   * on this device.
   *
   * <p>Sceneform requires Android N on the device as well as OpenGL 3.0 capabilities.
   *
   * <p>Finishes the activity if Sceneform can not run
   */
  public static boolean checkIsSupportedDeviceOrFinish(final Activity activity) {
    if (Build.VERSION.SDK_INT < VERSION_CODES.N) {
      Log.e(TAG, "Sceneform requires Android N or later");
      Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG).show();
      activity.finish();
      return false;
    }
    String openGlVersionString =
        ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE))
            .getDeviceConfigurationInfo()
            .getGlEsVersion();
    if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
      Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later");
      Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
          .show();
      activity.finish();
      return false;
    }
    return true;
  }
}
