# Charba showcase

This application uses Charba 6.5's Java core API, adapted by `charba-compat`, with
Elemental2 compatibility bindings for the surrounding page. Chart.js and Luxon
are embedded by Charba; there are no CDN requests.

Install the [build prerequisites](../DEVELOPMENT.md), then from the repository root, using JDK 21:

```sh
mvn -pl charba-demo -am verify
python3 -m http.server 8766 --bind 127.0.0.1 --directory charba-demo/target/site
```

Open <http://127.0.0.1:8766/>. Stop the server with Ctrl+C.
The first Maven build requires access to the configured dependency repositories.
The browser integration tests use Playwright's Chromium and WebKit installations.

Choose bar, line, area, doughnut, radar or polar area from the sidebar. Each
example includes a Java snippet and a link to its full generated source.
The page offers **Update data**, **Destroy chart** and **Recreate chart**. Click
inside the plot to exercise a Java handler invoked from Chart.js. Alternating
bar colors come from a Java scriptable color callback. Resizing the window
exercises the chart's responsive sizing.

`verify` runs the browser contract in Chromium and WebKit. It checks rendered
pixels, resolved callback colors, click events, data updates, resize, repeated
cleanup/recreation and absence of JavaScript errors. The demo also checks that
destroyed charts leave both Charba registries. Screenshots are written to
`target/chart-demo-chromium.png` and `target/chart-demo-webkit.png`.

See [port scope and lifecycle notes](../charba-compat/README.md).

Each example is also captured in `target/showcase-{example}-{engine}.png`.
The suite checks switching examples leaves exactly one live Chart.js instance.
