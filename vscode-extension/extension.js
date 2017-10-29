// The module 'vscode' contains the VS Code extensibility API
// Import the module and reference it with the alias vscode in your code below
const vscode = require('vscode');
const craft = require('./craft.js');

// this method is called when your extension is activated
// your extension is activated the very first time the command is executed
function activate(context) {
    // Use the console to output diagnostic information (console.log) and errors (console.error)
    // This line of code will only be executed once when your extension is activated
    console.log('Congratulations, your extension "nctu-hack" is now active!');

    // The command has been defined in the package.json file
    // Now provide the implementation of the command with  registerCommand
    // The commandId parameter must match the command field in package.json
    let disposable = vscode.commands.registerCommand('extension.sayHello', function () {
        // The code you place here will be executed every time your command is executed
        var editor = vscode.window.activeTextEditor;
        if (!editor) {
            return; // No open text editor
        }
        
        var selection = editor.selection;
        var text = editor.document.getText(selection);
        
        // Display a message box to the user
        vscode.window.showInformationMessage('Selected characters: ' + text.length);
    });
    
    craft.connect("Code.exe","");
    var preTouchEvent = false;
    var preTouchState = -1;
    craft.onEvent(function(event) {
      console.log(event);
      if(event.message_type == 'crown_touch_event') {
        var editor = vscode.window.activeTextEditor;
        if(editor &&　event.touch_state == 0 && preTouchEvent == true && preTouchState == 1) {
          var selection = editor.document.getText(editor.selection);
          if(selection.length != 0) {
            var fileName = editor.document.fileName;
            console.log("--> selection: " + selection);
            console.log("--> fileName: " + fileName);
            // show dialog
            var exec = require('child_process').exec;
            exec("java -jar C:\\Users\\Alan\\Desktop\\nctu-hack\\java-popup\\build\\libs\\java-popup-1.0.jar " + selection + " " + fileName, 
              function puts(error, stdout, stderr) {
                console.log(stderr);
                console.log(error);
                console.log(stdout);
              }
            );
          }
        }
        preTouchEvent = true;
        preTouchState = event.touch_state;
      } else {
        preTouchEvent = false;
        preTouchState = -1;
      }
    });    

    context.subscriptions.push(disposable);
}
exports.activate = activate;

// this method is called when your extension is deactivated
function deactivate() {
}
exports.deactivate = deactivate;