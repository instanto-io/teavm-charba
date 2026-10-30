# Charba for TeaVM

This project brings the **Charba 6.5 core Java API** to TeaVM's JavaScript backend.
It keeps the `org.pepstock.charba.client` packages and adapts the upstream source
archive during the Maven build, using the shared TeaVM compatibility layer.
Chart.js remains the chart engine and draws through the browser's Canvas 2D API.

## Viewing the showcase

The showcase contains bar, line, area, doughnut, radar and polar-area charts,
with Java source snippets, data updates and destroy/recreate controls. Its bar
example uses a Java callback to choose colors. Chart.js and Luxon are embedded
resources; the page needs no CDN requests.

With the prerequisites in [Development](DEVELOPMENT.md) installed:

```sh
mvn clean verify
python3 -m http.server 8766 --bind 127.0.0.1 --directory charba-demo/target/site
```

Open <http://127.0.0.1:8766/>. The same static directory can be served by an
application web server. See the [showcase guide](charba-demo/README.md) for browser
verification and the [core guide](charba-compat/README.md) for initialization,
ownership and adaptation limits.

## Maven modules and coordinates

| Module | Purpose |
| --- | --- |
| `charba-compat` | Charba core sources adapted to TeaVM JSO |
| `charba-demo` | Six-example browser showcase and Chromium/WebKit contracts |

```xml
<dependency>
  <groupId>io.instanto</groupId>
  <artifactId>charba-compat</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

Use this artifact instead of `org.pepstock:charba`; including both defines the
same classes twice. The repository name is `teavm-charba`; the artifact remains
`charba-compat`. The separate upstream GWT widget distribution is not included.

## Automated snapshots

The complete reactor and Chromium/WebKit browser contracts are verified before
main-branch snapshots are published to packages.instanto.io. See
[CI and publication](DEVELOPMENT.md#continuous-integration-and-publication) for
credentials, the inherited org POMs and the demo’s publication scope.

## Compatibility and ownership

All 1,742 core Java source files compile. The browser suite verifies the six
showcase charts, Java callbacks, data changes, resizing and repeated cleanup.
Optional plugin APIs compile but have not all been exercised in the browser.
This is not a JVM chart renderer or a WebAssembly port.

Call `Charba.enable()` before creating charts and `chart.destroy()` when the
owning view is disposed. Remove the host element and release application-held
references too. Page-wide lifecycle listeners need separate removal when their
owner is disposed. Shared scripts remain loaded for the page lifetime.

A browser scene can host the chart element inside a `DomOverlayNode`, creating
on attach, resizing with layout, and destroying on detach. The Sarto Scene
showcase owns that integration; this library does not depend on Sarto Scene or
replace its portable canvas renderer.

## Source and licensing

[Charba](https://github.com/pepstock-org/Charba) is the upstream project. Maven
pins `org.pepstock:charba:6.5:sources`; generated adaptations live under `target/`.
No edited copy of upstream Java sources is maintained here. See [NOTICE](NOTICE)
and [LICENSE](LICENSE). Upstream license and notice files are packaged in the
core JAR. Embedded JavaScript retains its upstream notices.

The built showcase includes a visible **Licenses and notices** link and ships the full upstream license texts alongside its JavaScript. `legal/runtime/provenance.json` records the sources and checksums of runtime notices; refresh these copies when upgrading dependencies. Keep the complete `target/site` directory when publishing or redistributing the showcase.

## Quality

SpotBugs runs on every build. High-priority findings fail the build, and all findings are published to [code scanning](https://github.com/instanto-io/teavm-charba/security/code-scanning). SonarQube analysis runs on `main` against the organisation's private instance; maintainers see the gate, coverage and issue counts in the workflow summary. The Sonar quality gate is advisory, so a snapshot still publishes when only that gate fails.

See [Actions](https://github.com/instanto-io/teavm-charba/actions) for the latest verification and quality summary.

