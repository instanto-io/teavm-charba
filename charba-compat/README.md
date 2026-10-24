# Charba core for TeaVM

`io.instanto:charba-compat:0.1.0-SNAPSHOT` adapts the published **Charba 6.5 core**
source archive with the shared JsInterop binding generator. It retains the
`org.pepstock.charba.client` API and packages the upstream license and notices.
The build downloads the pinned source JAR from Maven Central; generated sources
stay under `target/`. No edited copy of Charba is maintained here.

Use this artifact **instead of** `org.pepstock:charba` in a TeaVM application.
Do not include both: they define the same classes. This is the core distribution;
the separate upstream `6.5-gwt` widget distribution is not included.

```xml
<dependency>
  <groupId>io.instanto</groupId>
  <artifactId>charba-compat</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

The application also needs TeaVM's class library and its usual TeaVM build plugin.
See [the complete demo](../charba-demo) for the working Maven configuration.

## Initialize and attach

Call `Charba.enable()` before constructing charts. Charba embeds Chart.js, its
helper scripts and the Luxon date adapter/library as Java resources, and injects
them at runtime. The demo needs no CDN or separate JavaScript download.
`Charba.enable(false)` means that the application supplies the date library;
it does not disable all date-adapter initialization.

Create and configure a chart, then append `chart.getChartElement().as()` to the
page. With `setDrawOnAttach(true)`, Charba's MutationObserver draws after the
DOM insertion. A synchronous `draw()` immediately after insertion may precede
that notification. Use a `ChartsLifecycleListener` if initialization completion
matters to your UI.

## Ownership and cleanup

Call `chart.destroy()` when permanently removing a chart. This releases its
Chart.js instance, native listeners, registrations and Charba observer entry.
Then remove its host element and release your own chart/dataset references.
Recreate a new Java chart object when needed. Remove application-installed
`ChartsLifecycleListener`s when the owning application/view is disposed; those
listeners belong to Charba's global registry, not to an individual chart.

Charba's shared resources and shared DOM observer live for the page lifetime.
Destroying one chart does not unload them.

## Scope

All 1,742 source files in the core archive are adapted and Java-compiled, including
its optional plugin APIs. Browser coverage is deliberately narrower: the demo
exercises bar, line, area, doughnut, radar and polar-area charts, scriptable colors,
Java click handlers, data updates,
responsive resize and repeated destruction/recreation. Compiling an optional
plugin is not evidence that all of its behavior has been tested.

The port targets TeaVM's **JavaScript backend**. WebAssembly and the GWT widget
artifact have not been validated. The upstream embedded scripts require permission
to inject scripts into the host page; a strict CSP may require a different resource
loading arrangement.

The shared adapter resolves string constants and string concatenations in
JsInterop `name`/`namespace` attributes, including imported and local constants.
Constants must be available in the adapted source set (apart from the built-in
`JsPackage.GLOBAL`). Unsupported expressions fail generation with a diagnostic.

Generic native setters can erase a `@JsFunction` argument to `Object`. The
adapter preserves such callbacks at calls through a field, parameter or local
variable with a concrete function type argument. It generates a typed bridge,
converts boxed values at the boundary, and weakly caches the native function to
preserve identity. This supports Charba's `CallbackProxy<T>` pattern without
changing the upstream library. Overloaded setters, inherited setters and
receivers returned by arbitrary expressions require an explicit bridge today.
