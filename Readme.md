# SudoQ

Times of bringing towels along are long over, changes are demanded.

I do enjoy using the SudoQ application, but some things have to be changed (or even removed).

## Why does this fork exist?

This fork has two main purposes:

- One of course is to fix encountered bugs and pull request them.
- The other is to enhance the usability in a way that it would change a lot of code and hence probably not fit into the main project.

So this fork is primarily created to improve this app for my usage.\
But I am willed to also improve the official application. Given the current maintainer likes to collaborate.

### Needed features:

There are a few things to improve in this app which would significantly improve its quality: 

- **Inputs**:\
    Currently to place a number into a field, one first selects the field and then places the number into it.\
    This might be fine for normal fields. However when working with notes it is very common, to place the same number into several fields in a rapid fashion.\
    On top of having to press each of the fields per note, one also has to double tap it to switch to note input mode.\
    A better solution would be to select the number first and then place it directly by tapping fields. This also helps in early game.\
    This is my mostly desired feature. With it, this applications usability would already increase dozen times.
    
- **HelpSystem**:\
    Show remaining field count (in the top bar).\
    Show how many of each number are still missing (like a small indicator in the buttons).\
    Highlight digits when another of its kind is selected (or chosen as input digit).
    
- **History**:\
    When placing a number which would remove notes, this is counted as multiple history steps, while it should be only one visible step.\
    It makes using undo mentally challenging (several instead of just a single press).
    
- **Looks**:\
    Currently there is no theme, everything is green-white.\
    While the green is fine, a dark mode should be a thing these days.\
    Probably one theme for the Sudoku, and one for the app.
    
- **Score**:\
    I do not care nor use the score - But the penalty should be action based and not settings based.\
    So that for example jumping back to a bookmark or revealing a field does give a penalty in score points.\
    It should also be possible to enable/disable help while playing (or at least more easily).

## What is the state of this fork?

#### Entry point:

The application was very stable when I first used it.\
My first PR was only a typo fix. But as time went on, I found several issues outside of my use-case.

4 to 5 months before this fork was created, the app migrated from Java to Kotlin.\
However in that process other things got changed and several new bugs appeared.\
On top the Git commits are not really describing which change was made and why.\
Hence I decided to roll this fork back to before the migration.\
This should be fine, since the app was fairly stable then.

I decided to remove the tests. While this is in general a bad decision.\
It is less code to refactor for me. Since I do plan a bit of refactoring.\
The tests can be re-added later on, depending on which decisions have been made.

#### Done:

- The language support has been fully refactored, now it should no longer have any issues.
- This fork is pretty much up to date with the parent project. Except some refactorings by the maintainer.

#### Planned:

- More refactoring to clean up the code. (This is basically an ongoing TODO).
- The input system should be changed. Ideally in a way that one can choose between the old and the new system. (Field vs Digit selection first).
- Probably change the way activities communicate. Currently there is a lot of static field access going on.

## Import/Use:

- Download/Install/Setup AndroidStudio
- Clone this repository
- Import the downloaded repository with AndroidStudio

## Contact:

Please contact me to talk about the app and this project :)

An easy way would be to use the GitHub Discussions tab of this project.\
But if it should be less public, just use Discord.

The easiest way to contact me is to join my [Discord server](https://discord.gg/dYYxNvp).\
I do have a channel there dedicated to this project where I put project state updates.
