# Development

## Build and run

Use **JDK 21** and Maven 3.9 or newer. Java sources target Java 17; the build
and formatting tools require JDK 21. This is a snapshot port, not a Maven Central
release. It depends on the Instanto TeaVM parent/toolchain and the matching
[teavm-compat](https://github.com/instanto-io/teavm-compat) snapshots.
Install those prerequisites using their repository instructions before building
this repository. In a sibling checkout, install the compatibility reactor with:

```sh
mvn -f ../teavm-compat/pom.xml install
```

The first build needs access to the configured Maven repositories. Parent and
TeaVM snapshot resolution uses the same Maven settings as the other Instanto
ports. GitHub source publication does not publish these Maven artifacts.

```sh
mvn clean verify
mvn install
python3 -m http.server 8766 --bind 127.0.0.1 --directory charba-demo/target/site
```

Open <http://127.0.0.1:8766/>; stop the server with Ctrl+C. `package` produces
the static demo; `verify` also runs Playwright integration tests in Chromium and
WebKit. Install both browser engines before verification if necessary:

```sh
mvn -pl charba-demo dependency:build-classpath -Dmdep.outputFile=target/test-classpath.txt
java -cp "$(cat charba-demo/target/test-classpath.txt)" com.microsoft.playwright.CLI install chromium webkit
```

On Linux the engines also require Playwright's documented system dependencies.
Generated sources, JavaScript, source maps, reports and screenshots stay under
`target/` and are not committed. `mvn install` is local; it does not deploy.

## Verification and contribution

Run `mvn clean verify` before committing. The build applies Google Java Format,
compiles the Java API, creates source/Javadoc artifacts, runs browser contracts,
and checks high-priority SpotBugs findings. Inspect the diff after formatting.
Browser success covers the scenarios documented in the demo, not every upstream
API. Keep new compatibility transformations in `teavm-compat` with focused tests;
keep library-specific API corrections and examples in this repository.

This repository currently has no automated release workflow. Snapshot artifacts
can be deployed only after configuring credentials for the `github` server ID;
the root POM points at this repository's GitHub Packages registry. No credential
values belong in source control. Do not publish a release or claim a supported
version without rerunning the browser contracts and recording the upstream baseline.
