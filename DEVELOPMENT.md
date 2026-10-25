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

## Continuous integration and publication

Every push to `main`, trusted pull request and manual workflow run verifies the
complete reactor, including the Chromium and WebKit contracts in `charba-demo`.
CI uses JDK 21, the browser-equipped Instanto container runners, and a fresh Maven
repository so locally installed snapshots cannot hide missing dependencies.
Playwright downloads its matching browser revisions into a cache for each job;
the runner image supplies their OS libraries. Both Maven and browser caches are
removed after verification.
Fork pull requests need a maintainer to move the reviewed change to a trusted branch.

After verification succeeds on `main`, the same workflow revision runs `clean deploy`
through the shared `instanto-poms` Maven workflow. The parent chain is
`teavm-charba-parent` → `instanto-teavm-pom` → `instanto-org-pom`; its
`forgejo-instanto` snapshot repository is packages.instanto.io. The workflow needs
`FORGEJO_PACKAGE_USER` and a package-write `FORGEJO_PACKAGE_TOKEN` (or
`FORGEJO_PACKAGES_TOKEN`). Secrets are supplied by GitHub, never stored in the POM.

Publication includes the parent POM and `charba-compat` artifacts. The full demo is
built and tested, but retains `maven.deploy.skip`: its `target/site` is a static
website, not a library release. This workflow does not deploy the website.
A manual run on `main` repeats verification before publishing; pull requests and
manual runs on other branches do not publish snapshots.

For local publication, configure the same server credentials and run:

```sh
mvn --settings .github/maven-settings.xml clean deploy
```

The checked-in settings bootstrap parent/plugin resolution and reference credentials
through environment variables. An org POM defines Maven conventions and destinations;
it cannot create or trigger a repository's GitHub Actions workflow. Published
`teavm-compat` dependencies are prerequisites; CI deliberately fails if they are absent.
