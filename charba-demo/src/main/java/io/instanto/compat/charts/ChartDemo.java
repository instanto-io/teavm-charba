package io.instanto.compat.charts;

import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import org.pepstock.charba.client.*;
import org.pepstock.charba.client.data.*;
import org.pepstock.charba.client.enums.Fill;
import org.pepstock.charba.client.events.ChartClickEvent;
import org.pepstock.charba.client.events.ChartClickEventHandler;

/** Interactive examples of the upstream Charba Java API adapted to TeaVM. */
public final class ChartDemo {
  private static final String[] EXAMPLES = {"bar", "line", "area", "doughnut", "radar", "polar"};
  private static final String[] COLORS = {"#4f46e5", "#14b8a6", "#f59e0b", "#ec4899", "#60a5fa"};
  private static AbstractChart chart;
  private static Dataset dataset;
  private static String selected = "bar";
  private static int revision;
  private static int clicks;

  public static void main(String[] args) {
    Charba.enable();
    // One listener owned by this page. Each example owns and destroys its individual chart.
    Charts.addLifecycleListener(
        new ChartsLifecycleListener() {
          @Override
          public void onAfterInit(IsChart initialized) {
            if (initialized == chart) status("Ready");
          }
        });
    for (String example : EXAMPLES) {
      DomGlobal.document
          .getElementById("example-" + example)
          .addEventListener(
              "click",
              event -> {
                destroy();
                selected = example;
                create();
              });
    }
    create();
    DomGlobal.document
        .getElementById("update")
        .addEventListener(
            "click",
            event -> {
              if (chart == null) return;
              revision++;
              dataset.setData(
                  12 + revision, 19 + revision, 8 + revision, 15 + revision, 22 + revision);
              chart.update();
              status("Updated · revision " + revision);
            });
    DomGlobal.document.getElementById("destroy").addEventListener("click", event -> destroy());
    DomGlobal.document
        .getElementById("recreate")
        .addEventListener(
            "click",
            event -> {
              destroy();
              create();
            });
  }

  private static void create() {
    String title;
    String description;
    String snippet;
    switch (selected) {
      case "line", "area" -> {
        LineChart line = new LineChart();
        LineDataset series = line.newDataset();
        series.setBorderColor("#4f46e5");
        series.setBackgroundColor("rgba(79,70,229,0.16)");
        series.setBorderWidth(3);
        series.setTension(0.35);
        series.setFill(selected.equals("area") ? Fill.ORIGIN : Fill.FALSE);
        chart = line;
        dataset = series;
        title = selected.equals("area") ? "Area chart" : "Line chart";
        description =
            selected.equals("area")
                ? "Show a trend and its volume with a smooth line filled down to the baseline."
                : "Follow a series over time. Hover over a point to inspect its value.";
        snippet =
            "LineChart chart = new LineChart();\nLineDataset series = chart.newDataset();\nseries.setTension(0.35);\nseries.setFill(Fill."
                + (selected.equals("area") ? "ORIGIN" : "FALSE")
                + ");";
      }
      case "doughnut" -> {
        DoughnutChart ring = new DoughnutChart();
        DoughnutDataset series = ring.newDataset();
        series.setBackgroundColor(COLORS);
        chart = ring;
        dataset = series;
        title = "Doughnut chart";
        description = "Compare parts of a whole. Click a legend entry to hide or show a segment.";
        snippet =
            "DoughnutChart chart = new DoughnutChart();\nDoughnutDataset series = chart.newDataset();\nseries.setBackgroundColor(\"#4f46e5\", \"#14b8a6\",\n    \"#f59e0b\", \"#ec4899\", \"#60a5fa\");";
      }
      case "radar" -> {
        RadarChart radar = new RadarChart();
        RadarDataset series = radar.newDataset();
        series.setBackgroundColor("rgba(20,184,166,0.20)");
        series.setBorderColor("#0d9488");
        chart = radar;
        dataset = series;
        title = "Radar chart";
        description = "Compare several dimensions on a shared radial scale.";
        snippet =
            "RadarChart chart = new RadarChart();\nRadarDataset series = chart.newDataset();\nseries.setBorderColor(\"#0d9488\");\nseries.setBackgroundColor(\"rgba(20,184,166,0.20)\");";
      }
      case "polar" -> {
        PolarAreaChart polar = new PolarAreaChart();
        PolarAreaDataset series = polar.newDataset();
        series.setBackgroundColor(COLORS);
        chart = polar;
        dataset = series;
        title = "Polar area chart";
        description = "Equal angles, different radii: a radial view of five values.";
        snippet =
            "PolarAreaChart chart = new PolarAreaChart();\nPolarAreaDataset series = chart.newDataset();\nseries.setBackgroundColor(\"#4f46e5\", \"#14b8a6\",\n    \"#f59e0b\", \"#ec4899\", \"#60a5fa\");";
      }
      default -> {
        BarChart bars = new BarChart();
        BarDataset series = bars.newDataset();
        series.setBackgroundColor(
            context -> context.getDataIndex() % 2 == 0 ? "#4f46e5" : "#14b8a6");
        chart = bars;
        dataset = series;
        title = "Bar chart";
        description = "Compare daily totals. A Java callback chooses the color of each bar.";
        snippet =
            "BarChart chart = new BarChart();\nBarDataset series = chart.newDataset();\nseries.setBackgroundColor(context ->\n    context.getDataIndex() % 2 == 0 ? \"#4f46e5\" : \"#14b8a6\");";
      }
    }
    chart.setDrawOnAttach(true);
    chart.getOptions().setResponsive(true);
    chart.getOptions().setMaintainAspectRatio(false);
    chart.getOptions().getAnimation().setDuration(0);
    chart.getData().setLabels("Mon", "Tue", "Wed", "Thu", "Fri");
    dataset.setLabel("Library builds");
    dataset.setData(12, 19, 8, 15, 22);
    chart.getData().setDatasets(dataset);
    chart.addHandler(
        (ChartClickEventHandler)
            event -> {
              clicks++;
              DomGlobal.document.getElementById("clicks").textContent = "Chart clicks: " + clicks;
            },
        ChartClickEvent.TYPE);
    for (String example : EXAMPLES) {
      Element button = DomGlobal.document.getElementById("example-" + example);
      button.setAttribute("aria-pressed", Boolean.toString(example.equals(selected)));
    }
    DomGlobal.document.getElementById("example-title").textContent = title;
    DomGlobal.document.getElementById("description").textContent = description;
    DomGlobal.document.getElementById("source").textContent =
        snippet
            + "\n\nseries.setLabel(\"Library builds\");\nseries.setData(12, 19, 8, 15, 22);\nchart.getData().setLabels(\"Mon\", \"Tue\", \"Wed\", \"Thu\", \"Fri\");\nchart.getData().setDatasets(series);\nchart.setDrawOnAttach(true);\nhost.appendChild(chart.getChartElement().as());";
    DomGlobal.document.getElementById("chart").appendChild(chart.getChartElement().as());
    revision = 0;
    status("Drawing…");
  }

  private static void destroy() {
    if (chart != null) {
      String id = chart.getId();
      chart.destroy();
      if (Charts.get(id) != null || Charts.hasNative(id))
        throw new IllegalStateException("Destroyed chart is still registered");
      chart.getChartElement().remove();
      chart = null;
      dataset = null;
    }
    status("Destroyed");
  }

  private static void status(String value) {
    DomGlobal.document.getElementById("status").textContent = value;
  }
}
