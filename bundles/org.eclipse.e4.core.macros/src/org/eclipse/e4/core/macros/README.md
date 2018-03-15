# To be added to https://wiki.eclipse.org/E4/Macros when integrated.

Basic information on how macros should work and what is expected of clients.

The basic idea in this implementation is that the macro plugin is a thin layer
used to record macro commands and then play it back. The actual work is done
on other plugins, which should be macro-aware.

The macro recording doesn't only map to text editions. Anything done in the
IDE could be recorded in a macro to be played back later on. For instance,
it may be possible to record and playback changes to preferences, importing
some git repository, finding text, etc. Later on, when playing it back, it
should be even possible to ask if the user wants to edit some of the values
which were saved during record.

Still, note that while that support will be possible, currently the
implementation only targets the text editor and its operations (whitelisting
only "safe" commands).

Abstract structure
================================================

The basic idea is that editors listen or detect when macro record mode is active
and upon entering macro mode, they make sure actions are properly tracked so
that they can be played back accordingly.

-- note that it's important that things are not the other way around: the
macro plugin is a thin layer to start/stop macro recording and provide the basic
abstractions and management of macros, it's up to the clients
to be aware that they are in macro record mode and act accordingly,
issuing commands to be recorded and later played back.

e.g.: some editors may add a closing quote or closing parenthesis
automatically. While it's Ok for them to make that in record mode, they
have to make sure that a proper action is recorded for this case and later
on when it's played back the same behavior is done. Things such as
auto-completion should also respect that (or be disabled in record mode).

Actual implementation
======================

Clients can either listen for when a macro recording will start/finish and when
a macro playback will start/finish or just query the current state.

-- Note: it's possible for the user to start a record mode and playback
a previous macro in such a mode (although the opposite is not true).

Scenario 1:

Setting up state when entering macro record/playback mode:

This gives an example where some state must be set for both macro and record
mode (i.e.: disabling code-completion) and in the record mode it also has to
record keypresses to be played back later on.

```
public class MyMacroAwareClass implements IMacroContextListener {

    public void init(final IEditorSite site) {
        fMacroContext = site.getService(EMacroContextService.class);
        if (fMacroContext != null) {
            fMacroContext.addMacroListener(this);
            if (fMacroContext.isRecording() || fMacroContext.isPlayingBack()) {
                this.macroStateChanged(fMacroContext);
            }
        }
    }

    public final void macroStateChanged(EMacroContextService macroContext) {
        boolean isInRecordOrPlaybackMode = macroContext.isRecording() || macroContext.isPlayingBack();
        if (isInRecordOrPlaybackMode && !fInternalStateForRecordPlaybackMode) {
            // entered macro record or playback
            fInternalStateForRecordPlaybackMode = true;
            this.disableCodeCompletion();
            if(macroContext.isRecording()){
	            this.startRecordingKeyEvents(macroContext);
            }
        } else if (!isInRecordOrPlaybackMode && fInternalStateForRecordPlaybackMode) {
            // exited macro record or playback
            fInternalStateForRecordPlaybackMode = false;
            this.enableCodeCompletion();
            if(!macroContext.isRecording()){
            	this.endRecordingKeyEvents();
        	}
        }
    }

    protected void startRecordingKeyEvents(final EMacroContextService macroContext){
    	this.fKeyEventListener = new Listener(){
			@Override
			public void handleEvent(Event event) {
				if (event.type == SWT.KeyDown && fMacroContext.isRecording()) {
					fMacroContext.addMacroCommand(new KeyDownMacroCommand(event));
				}
			}
    	};
    	this.addListener(SWT.KeyDown, this.fKeyEventListener);
    }

    protected void endRecordingKeyEvents(){
    	this.removeListener(SWT.KeyDown, this.fKeyEventListener);
    }

}
```

An actual implementation using this strategy is:

org.eclipse.ui.texteditor.AbstractTextEditor

-- Note: editors inheriting AbstractTextEditor will actually have their code completion
disabled in record and playback mode by default and will also record key presses
in their StyledText by default. It's possible to customize such behavior in
subclasses by overwriting the org.eclipse.ui.texteditor.AbstractTextEditor.getDisableContentAssistOnMacroRecord()
method.



Scenario 2:

Just issuing commands when no setup is needed:

```
public class MyMacroAwareClass implements IMacroContextListener {

    @Inject
    EMacroContextService fMacroContext;

    public void runCommand(String macroCommandId) {
    	... // Actually run command
        if(fMacroContext.isRecording()){
            fMacroContext.addCommand(new RunCommandMacroCommand(macroCommandId));
        }
    }
}
```

An actual implementation using this strategy is:

org.eclipse.e4.ui.bindings.keys.KeyBindingDispatcher

-- Note: commands registered through the eclipse commands extensions will be already
blacklisted by default if they aren't registered in the org.eclipse.e4.ui.macros.acceptedCommands
extension point and their activation will already be recorded for proper playback by default
(so, clients only actually need to customize actions which currently aren't implemented as
eclipse actions).

Macro playback
===============

After a command is actually added to the EMacroContextService and a record session is finished, all the recorded
IMacroCommands will be saved to disk using the contents of their IMacroCommand#toMap() method to enable
playing it back later on.

Afterward, when it's time to play a macro command back, the contents of the map are gotten from disk
and will be recreated through factories registered in the org.eclipse.e4.core.macros.macroCommandsFactory
extension point (so it's important to note that if a custom command is created, a factory to recreate it
must be registered).

On playback, the macro command must perform the same action which was recorded (so, for instance, if
a command to delete the current line of the editor was issued, on playback the current line of the
current editor must be deleted).


