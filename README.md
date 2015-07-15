# Contacts Generator for Android (WorkInProgress)

A simple contacts generating app (with images)

Summary (what I want it to do in the end)
-----------------------------------------
- Choose a number of contacts to generate
- Choose whether to use contact photos or not
- Work in background so you get a notification when it's done

[ This will probably use uinames.com and/or randomuser.me as public APIs ]

*Note: API key stored in the **raw** folder is a public one. We keep the private one locally.
When modifying the project, be sure to exclude that file either in your **local** version
of the **.gitignore** file, or using the following **git** commands:*

To stop tracking files
``` git update-index --assume-unchanged FILENAME ```

To start tracking files
``` git update-index --no-assume-unchanged FILENAME ```