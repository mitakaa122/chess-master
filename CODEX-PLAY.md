# Play against Codex

Use JDK 11 and the Windows x64 JavaFX SDK 11.0.2 from [Gluon](https://gluonhq.com/products/javafx/). Extract the SDK so that `sdk/javafx-windows/javafx-sdk-11.0.2/lib/javafx.controls.jar` exists. Downloaded SDKs are excluded from Git.

Run `mvn dependency:resolve` once to populate the Maven dependency cache. With JDK 11 on `PATH`, run:

```powershell
.\test.ps1
.\run.ps1 -Build
```

`build.ps1` uses the annotations and commons-codec dependencies already cached by Maven in the current user's `.m2` directory. `test.ps1` tests the independent rules engine without JavaFX dependencies.

On the original development PC, the personal `java11` command selects JDK 11 for the current terminal; this command is not required on other machines.

The default game is **White: you, Black: Codex**. Click a white piece and then its destination. Legal destinations are highlighted. The status line reports the turn, check and game outcome. Codex chooses moves during the active conversation; no autonomous chess bot is running.

The original piece artwork is retained. The active rules engine is `Rules.java`; old movement classes remain for compatibility with the original piece model but no longer validate gameplay.

## Codex interface

```powershell
.\agent.ps1 state
.\agent.ps1 move -Move e7e5 -Version 1
```

Use the exact `version` from the latest state and a UCI move from its `legalMoves`, including a promotion suffix when needed, such as `a2a1n`. Only Black's legal moves are accepted. Requests run on the JavaFX UI thread, so both interfaces share the same state. The server listens only on loopback and requires the token in `target/codex-session.properties`; `target/` is excluded from Git. Starting a new Codex game rotates the port and token.

**Game** contains new Codex/local games, undo/redo and draw claims. In Codex mode undo normally takes back a full pair of moves. Promotion offers queen, rook, bishop or knight. Stalemate, checkmate, basic insufficient material, fivefold repetition and the 75-move rule end the game. Threefold repetition and the 50-move rule can be claimed in the current position; advance claims based on a proposed move are not implemented. Board size is fixed in this version.

## Validation status

The rules test covers initial-position perft through depth 4 (197,281 nodes), Kiwipete through depth 3 (97,862), a rook/en-passant endgame, king safety, castling, en passant, underpromotion, mate, stalemate and draw conditions.

`BridgeTest.java` is an integration test for JavaFX clicks and HTTP moves. It must compile and pass before the complete UI/bridge integration is considered verified. The isolated environment has reported access errors while closing local JavaFX JARs and moving the packaged JAR from its temporary directory. On October 13, 2026, all 24 rules checks and Java compilation passed, but JAR packaging failed with `AccessDeniedException`. Full UI/bridge integration remains unverified.
