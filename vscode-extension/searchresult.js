const vscode = require('vscode');

class SearchResult {
    
    constructor() {
        this._onDidChange = vscode.EventEmitter();
    }

    provideTextDocumentContent(uri) {
        return this.createCssSnippet();
    }

    get onDidChange() {
        return this._onDidChange.event;
    }

    update(uri) {
        this._onDidChange.fire(uri);
    }

    createCssSnippet() {
        let editor = vscode.window.activeTextEditor;
        let text = editor.document.getText();
        return this.snippet(text);
    }

    snippet(text) {
        return`
            <body>
                <h1>Title here....Title here....Title here....<h1>
                <div>
                    <p1> ${text} </p1>
                </div>
            </body>`;
    }
}

exports.SearchResult = SearchResult