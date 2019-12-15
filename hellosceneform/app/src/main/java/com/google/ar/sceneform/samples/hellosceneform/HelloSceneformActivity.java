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
import android.content.Intent;
import android.net.Uri;
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
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Texture;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.ux.TransformableNode;

/**
 * This is an example activity that uses the Sceneform UX package to make common AR tasks easier.
 */
public class HelloSceneformActivity extends AppCompatActivity {
    private static final String TAG = HelloSceneformActivity.class.getSimpleName();
    private static final double MIN_OPENGL_VERSION = 3.0;

    private enum GridType {
        FLOOR, WALL, TREE, BARREL;
    }
    /*private int[] grid = {
            1,1,1,1,1, 1,1,1,1,1,
            1,0,0,0,0, 0,0,0,0,1,
            1,0,0,0,0, 0,0,0,0,1,
            1,0,0,2,0, 0,2,0,0,1,
            1,0,0,0,0, 0,0,0,0,1,
            1,0,0,0,0, 0,0,0,0,1,
            1,0,0,0,0, 0,0,0,0,1,
            1,0,0,0,0, 0,0,0,0,1,
            1,0,0,0,0, 0,0,0,0,1,
            1,1,1,1,1, 1,1,1,1,1
    };*/

    private ArFragment arFragment;
    private boolean hasRendered = false; // Set it to false to prevent creating multiple models

    private final int COL = 10;
    private final float SIZE_SCALE = 1.5f;
    private final float CENTER_SCALE = 0.15f;
    private final float BASE_HEIGHT = 0.15f;
    private final int OFFSET = 5; // offset for anchor/plane

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

      // Get grid data from GridActivity scene
      Intent intent = getIntent();
      int[] grid = intent.getIntArrayExtra("gridValues");

      arFragment.setOnTapArPlaneListener((HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
          if (hasRendered) {
              return;
          }
          hasRendered = true;

          // Create the Anchor.
          Anchor anchor = hitResult.createAnchor();
          AnchorNode anchorNode = new AnchorNode(anchor);
          anchorNode.setParent(arFragment.getArSceneView().getScene());

          // Create the empty movable plane.
          TransformableNode planeNode = new TransformableNode((arFragment.getTransformationSystem()));
          planeNode.setParent(anchorNode);

          // Create the shapes
          for (int i = 0; i < grid.length; i++) {
              int x = (i % COL) - OFFSET;
              int z = (int) -Math.floor(i / COL) + OFFSET;

              Vector3 center = new Vector3((float) x * CENTER_SCALE, BASE_HEIGHT, (float) z * CENTER_SCALE);
              Vector3 base = new Vector3((float) x * CENTER_SCALE, 0.01f, (float) z * CENTER_SCALE);

              // Render floor
              if (grid[i] != GridType.WALL.ordinal()) {
                  Node shapeNode = new Node();
                  shapeNode.setParent(planeNode);

                  Texture.builder()
                          .setSource(getApplicationContext(), R.drawable.floor_texture)
                          .build()
                          .thenAccept(texture -> {
                              MaterialFactory.makeOpaqueWithTexture(this, texture).thenAccept(
                                      material -> {
                                          shapeNode.setRenderable(ShapeFactory.makeCube(
                                                  new Vector3(0.1f * SIZE_SCALE, 0.0f, 0.1f * SIZE_SCALE),
                                                  base,
                                                  material));

                                      });
                          });
              }

              // Render shapes
              Node shapeNode = new Node();
              shapeNode.setParent(planeNode);

              if (grid[i] == GridType.WALL.ordinal()) {
                  Texture.builder()
                          .setSource(getApplicationContext(), R.drawable.wall_texture)
                          .build()
                          .thenAccept(texture -> {
                              MaterialFactory.makeOpaqueWithTexture(this, texture).thenAccept(
                              material -> {
                                  shapeNode.setRenderable(ShapeFactory.makeCube(
                                          new Vector3(0.1f * SIZE_SCALE,0.2f * SIZE_SCALE,0.1f * SIZE_SCALE),
                                          center,
                                          material));
                              });
                      });
              } else if (grid[i] == GridType.BARREL.ordinal()) {
                  Texture.builder()
                          .setSource(getApplicationContext(), R.drawable.barrel_texture)
                          .build()
                          .thenAccept(texture -> {
                              MaterialFactory.makeOpaqueWithTexture(this, texture).thenAccept(
                                  material -> {
                                      shapeNode.setRenderable(ShapeFactory.makeCylinder(
                                              0.05f * SIZE_SCALE,
                                              0.15f * SIZE_SCALE,
                                              center,
                                              material));
                                  });
                          });

              } else if (grid[i] == GridType.TREE.ordinal()) {
                  ModelRenderable.builder()
                      // To load as an asset from the 'assets' folder ('src/main/assets/andy.sfb'):
                      //.setSource(this, Uri.parse("andy.sfb"))
                      .setSource(this, R.raw.lowpolytree)
                      .build()
                      .thenAccept(renderable -> {
                          shapeNode.setRenderable(renderable);
                          shapeNode.setLocalPosition(base);
                          shapeNode.setLocalScale(new Vector3(0.2f,0.25f,0.2f));
                      });

              }
          }
          planeNode.select();
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
