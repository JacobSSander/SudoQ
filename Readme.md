# SudoQ

Times of bringing towels along are long over, changes are demanded.

I do enjoy using the SudoQ application, but some things have to be changed (or even removed).

## Who is this fork for?

Primarily for myself, as I will ditch features that I do not need, but others might need.
I do not recommend others to use this fork. But feel free to contact me for other demands.

## Whats the roadmap of this fork?

After some thinking I decided to go back to a state where the code was not migrated to kotlin (~4months ago) for several reasons.
That means as next step the fixes/changes from the original repo have to be applied to the Java files.

I intend to cleanup several things, since it looks like this project does need a small refactoring. There are some unanswered questions in this code too.
Anything which is not required in this project should be removed. Primary this refers to old unused code and weird xml files.
Probably the test cases will be removed and recreated later on. That is less code to maintain - yeet yolo - lololooo.

Once the basics are working, new features will be added.

### Needed features:

- Input system: Currently a field is selected and then a number. However it should be the other way round, selecting a number and placing it into fields then. This has to be changed. The amount of wasted touch-clicks I have to do is insane without this.
- HelpSystem: Show remaining field count. Show remaining digit count.
- History: When placing a number which would remove notes, this is counted as multiple history steps, while it should be only one step. It makes using undo mentally challenging, instead of just a single press.
- Looks: Currently there is no theme, everything is green-white. While the green is fine, a dark mode should be a thing these days.
- Score: I do not care nor use the score - But the penalty should be action based and not settings based. It should also be possible to enable/disable help while playing.

## Import/Use:

- Download/Install/Setup AndroidStudio
- Clone this repository
- Import the downloaded repository with AndroidStudio

## Contact:

Please contact me to talk about the app and this project :)

The easiest way to contact me is to join my [Discord server](https://discord.gg/dYYxNvp).