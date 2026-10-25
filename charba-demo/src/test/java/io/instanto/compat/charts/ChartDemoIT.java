package io.instanto.compat.charts;

import static org.junit.Assert.*;

import com.microsoft.playwright.*;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.file.*;
import java.util.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class ChartDemoIT {
  @Parameterized.Parameters(name = "{0}")
  public static Object[] browsers() {
    return new Object[] {"chromium", "webkit"};
  }

  private final String engine;

  public ChartDemoIT(String engine) {
    this.engine = engine;
  }

  @Test
  public void rendersUpdatesAndReleasesCharts() throws Exception {
    Path site = Path.of("target/site").toAbsolutePath().normalize();
    var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext(
        "/",
        exchange -> {
          String request = exchange.getRequestURI().getPath();
          Path file =
              site.resolve(request.equals("/") ? "index.html" : request.substring(1)).normalize();
          if (!file.startsWith(site) || !Files.isRegularFile(file)) {
            exchange.sendResponseHeaders(404, -1);
            exchange.close();
            return;
          }
          byte[] bytes = Files.readAllBytes(file);
          exchange
              .getResponseHeaders()
              .set(
                  "Content-Type",
                  file.toString().endsWith(".js")
                      ? "application/javascript"
                      : "text/html; charset=utf-8");
          exchange.sendResponseHeaders(200, bytes.length);
          try (var output = exchange.getResponseBody()) {
            output.write(bytes);
          }
        });
    server.start();
    try (var playwright = Playwright.create();
        var browser =
            (engine.equals("webkit") ? playwright.webkit() : playwright.chromium()).launch()) {
      var page = browser.newPage(new Browser.NewPageOptions().setViewportSize(1100, 850));
      List<String> errors = new ArrayList<>();
      page.onPageError(errors::add);
      page.onConsoleMessage(message -> System.out.println("Browser: " + message.text()));
      page.navigate("http://127.0.0.1:" + server.getAddress().getPort());
      try {
        page.waitForFunction(
            "document.querySelector('#status').textContent === 'Ready'",
            null,
            new Page.WaitForFunctionOptions().setTimeout(15000));
      } catch (RuntimeException failure) {
        throw new AssertionError("Demo failed: " + errors, failure);
      }
      page.waitForFunction("Object.keys(Chart.instances).length === 1");
      assertEquals(1, page.locator("canvas").count());
      assertEquals(1, ((Number) page.evaluate("Object.keys(Chart.instances).length")).intValue());
      page.screenshot(
          new Page.ScreenshotOptions()
              .setPath(Path.of("target/chart-demo-" + engine + ".png"))
              .setFullPage(true));
      assertEquals(
          "#4f46e5",
          page.evaluate(
              "Object.values(Chart.instances)[0].getDatasetMeta(0).data[0].options.backgroundColor"));
      assertEquals(
          "#14b8a6",
          page.evaluate(
              "Object.values(Chart.instances)[0].getDatasetMeta(0).data[1].options.backgroundColor"));
      assertTrue(
          (Boolean)
              page.evaluate(
                  "(() => {const c=document.querySelector('canvas'); return c.getContext('2d').getImageData(0,0,c.width,c.height).data.some(v => v !== 0);})()"));
      page.locator("canvas").click();
      page.waitForFunction("document.querySelector('#clicks').textContent === 'Chart clicks: 1'");
      int width = ((Number) page.evaluate("Object.values(Chart.instances)[0].width")).intValue();
      page.setViewportSize(700, 850);
      page.waitForFunction("w => Object.values(Chart.instances)[0].width < w", width);
      page.locator("#update").click();
      assertEquals("Updated · revision 1", page.locator("#status").textContent());
      assertEquals(
          13,
          ((Number) page.evaluate("Object.values(Chart.instances)[0].data.datasets[0].data[0]"))
              .intValue());
      for (int i = 0; i < 5; i++) {
        page.locator("#destroy").click();
        assertEquals(0, page.locator("canvas").count());
        assertEquals(0, ((Number) page.evaluate("Object.keys(Chart.instances).length")).intValue());
        page.locator("#recreate").click();
        page.waitForFunction("Object.keys(Chart.instances).length === 1");
        assertEquals(1, page.locator("canvas").count());
        assertEquals(1, ((Number) page.evaluate("Object.keys(Chart.instances).length")).intValue());
      }
      page.locator("canvas").click();
      page.waitForFunction("document.querySelector('#clicks').textContent === 'Chart clicks: 2'");
      // Recreating a live chart must also release the old instance.
      page.locator("#recreate").click();
      page.waitForFunction("Object.keys(Chart.instances).length === 1");
      page.locator("#destroy").click();
      page.locator("#destroy").click();
      assertEquals(0, ((Number) page.evaluate("Object.keys(Chart.instances).length")).intValue());
      String[][] examples = {
        {"bar", "bar"},
        {"line", "line"},
        {"area", "line"},
        {"doughnut", "doughnut"},
        {"radar", "radar"},
        {"polar", "polarArea"}
      };
      for (String[] example : examples) {
        page.locator("#example-" + example[0]).click();
        page.waitForFunction("document.querySelector('#status').textContent === 'Ready'");
        assertEquals(1, ((Number) page.evaluate("Object.keys(Chart.instances).length")).intValue());
        assertEquals(example[1], page.evaluate("Object.values(Chart.instances)[0].config.type"));
        assertEquals("true", page.locator("#example-" + example[0]).getAttribute("aria-pressed"));
        assertFalse(page.locator("#source").textContent().isBlank());
        assertTrue(
            (Boolean)
                page.evaluate(
                    "(() => {const c=document.querySelector('canvas'); return c.getContext('2d').getImageData(0,0,c.width,c.height).data.some(v => v !== 0);})()"));
        page.locator("#update").click();
        assertEquals(
            13,
            ((Number) page.evaluate("Object.values(Chart.instances)[0].data.datasets[0].data[0]"))
                .intValue());
        page.screenshot(
            new Page.ScreenshotOptions()
                .setPath(Path.of("target/showcase-" + example[0] + "-" + engine + ".png"))
                .setFullPage(true));
      }
      page.locator("#destroy").click();
      assertEquals(0, ((Number) page.evaluate("Object.keys(Chart.instances).length")).intValue());
      assertTrue(errors.toString(), errors.isEmpty());
    } finally {
      server.stop(0);
    }
  }

  @org.junit.Test
  public void showcaseShipsCompleteLicenseLinks() throws Exception {
    java.nio.file.Path site = java.nio.file.Path.of("target/site");
    org.junit.Assert.assertTrue(
        java.nio.file.Files.readString(site.resolve("index.html"))
            .contains("href=\"legal/index.html\""));
    org.junit.Assert.assertEquals(
        java.nio.file.Files.readString(java.nio.file.Path.of("../LICENSE")),
        java.nio.file.Files.readString(site.resolve("legal/project/LICENSE")));
    org.junit.Assert.assertEquals(
        java.nio.file.Files.readString(java.nio.file.Path.of("../NOTICE")),
        java.nio.file.Files.readString(site.resolve("legal/project/NOTICE")));
    try (var pages = java.nio.file.Files.walk(site.resolve("legal"))) {
      for (var page : pages.filter(p -> p.toString().endsWith(".html")).toList()) {
        var links =
            java.util.regex.Pattern.compile("href=\"([^\"]+)\"")
                .matcher(java.nio.file.Files.readString(page));
        while (links.find()) {
          String href = links.group(1);
          if (!href.contains(":")) {
            org.junit.Assert.assertTrue(
                page + " has missing license link " + href,
                java.nio.file.Files.isRegularFile(page.getParent().resolve(href).normalize()));
          }
        }
      }
    }
  }
}
