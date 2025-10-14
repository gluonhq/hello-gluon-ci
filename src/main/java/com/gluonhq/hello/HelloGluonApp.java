/**
 * Copyright (c) 2019, 2025, Gluon
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of Gluon, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL GLUON BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.gluonhq.hello;

import com.gluonhq.attach.display.DisplayService;
import com.gluonhq.attach.statusbar.StatusBarService;
import com.gluonhq.attach.util.Platform;
import com.gluonhq.charm.glisten.application.AppManager;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.control.FloatingActionButton;
import com.gluonhq.charm.glisten.layout.Layer;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.gluonhq.charm.glisten.visual.Swatch;
import com.gluonhq.charm.glisten.visual.SwatchElement;
import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Dimension2D;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import static com.gluonhq.charm.glisten.application.AppManager.HOME_VIEW;

public class HelloGluonApp extends Application {

    private final AppManager appManager = AppManager.initialize(this::postInit);
    private FloatingActionButton fab;

    @Override
    public void init() {
        appManager.addViewFactory(HOME_VIEW, () -> {
            fab = new FloatingActionButton(MaterialDesignIcon.SEARCH.text,
                    e -> System.out.println("Search"));

            ImageView imageView = new ImageView(new Image(HelloGluonApp.class.getResourceAsStream("openduke.png")));

            imageView.setFitHeight(200);
            imageView.setPreserveRatio(true);

            Label label = new Label("Hello, Gluon Mobile!");
            VBox root = new VBox(20, imageView, label);
            root.setStyle("-fx-background-color: -primary-swatch-100;");
            root.setAlignment(Pos.CENTER);

            View view = new View(root) {
                @Override
                protected void updateAppBar(AppBar appBar) {
                    appBar.setTitleText("Gluon Mobile");
                }
            };

            fab.showOn(view);

            return view;
        });
    }

    @Override
    public void start(Stage stage) {
        appManager.start(stage);
    }

    private void postInit(Scene scene) {
        Swatch.LIGHT_GREEN.assignTo(scene);
        scene.getStylesheets().add(HelloGluonApp.class.getResource("styles.css").toExternalForm());

        if (Platform.isDesktop()) {
            Dimension2D dimension2D = DisplayService.create()
                    .map(DisplayService::getDefaultDimensions)
                    .orElse(new Dimension2D(640, 480));
            scene.getWindow().setWidth(dimension2D.getWidth());
            scene.getWindow().setHeight(dimension2D.getHeight());
        } else if (Platform.isAndroid()) {
            fixesForAndroid();
        }
    }

    public static void main(String[] args) {
        launch();
    }

    private Node fabNode;
    private final DoubleProperty bottomInset = new SimpleDoubleProperty();

    private void fixesForAndroid() {
        appManager.getGlassPane().getChildren().addListener((ListChangeListener<Node>) c -> {
            if (fabNode != null) {
                return;
            }
            while (c.next()) {
                if (c.wasAdded()) {
                    fabNode = c.getAddedSubList().stream()
                            .filter(n -> n instanceof Layer && !((Layer) n).getChildren().isEmpty())
                            .map(n -> ((Layer) n).getChildren().get(0))
                            .filter(n -> n instanceof Button && n.getStyleClass().contains("fab"))
                            .findFirst()
                            .orElse(null);
                    if (fabNode != null) {
                        fabNode.translateYProperty().bind(bottomInset);
                    }
                }
            }

        });
        AppBar appBar = appManager.getAppBar();
        DisplayService.create().ifPresent(service -> {
            service.systemBarsInsetsProperty().addListener((obs, ov, nv) -> {
                Insets padding = appBar.getPadding();
                appBar.setPadding(new Insets(nv.getTop(), padding.getLeft(), padding.getRight(), padding.getBottom()));
                bottomInset.set(-nv.getBottom());
            });
        });

        StatusBarService.create().ifPresent(service -> {
            boolean isAppBarDark = isDarkColor(appManager.getSwatch().getColor(SwatchElement.PRIMARY_500));
            boolean isSceneDark = isDarkColor(appManager.getSwatch().getColor(SwatchElement.PRIMARY_100));
            service.setSystemBarsAppearance(
                    isAppBarDark ? StatusBarService.APPEARANCE.LIGHT : StatusBarService.APPEARANCE.DARK,
                    isSceneDark ? StatusBarService.APPEARANCE.LIGHT : StatusBarService.APPEARANCE.DARK);
        });
    }

    private static boolean isDarkColor(Color color) {
        double brightness = 0.3 * color.getRed() + 0.59 * color.getGreen() + 0.11 * color.getBlue();
        return brightness < 0.5;
    }
}