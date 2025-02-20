package gui

import logic.Problem
import java.awt.event.KeyEvent
import java.util.function.Consumer

class MainHandler : MainFlow() {
    init {
        controlPanel.randomize.addActionListener {
            initialWorld = currentProblem.randomWorld()
            atomicWorld.set(initialWorld)
            worldPanel.repaint()

            editor.requestFocusInWindow()
        }

        controlPanel.goal.addActionListener {
            atomicWorld.set(initialWorld)
            worldPanel.repaint()

            executeGoal(currentProblem.goal)
            controlPanel.stepOver.isEnabled = false
            controlPanel.stepReturn.isEnabled = false

            editor.requestFocusInWindow()
        }

        controlPanel.problemPicker.addActionListener {
            controlPanel.startStopReset.text = "start"
            controlPanel.randomize.isEnabled = currentProblem.isRandom

            initialWorld = currentProblem.randomWorld()
            atomicWorld.set(initialWorld)
            worldPanel.binaryLines = currentProblem.binaryLines
            worldPanel.repaint()

            story.loadFromString(currentProblem.story)

            editor.setCursorTo("""\bvoid\s+(${currentProblem.name})\b""", 1)
            editor.requestFocusInWindow()
        }

        controlPanel.startStopReset.addActionListener {
            when (controlPanel.startStopReset.text) {
                "start" -> parseAndExecute()

                "stop" -> stop()

                "reset" -> {
                    controlPanel.startStopReset.text = "start"
                    atomicWorld.set(initialWorld)
                    worldPanel.repaint()
                }
            }
            editor.requestFocusInWindow()
        }

        controlPanel.check.addActionListener {
            controlPanel.startStopReset.text = "reset"
            checkAgainst(currentProblem.goal)

            editor.requestFocusInWindow()
        }

        controlPanel.stepInto.addActionListener {
            stepInto()
        }

        controlPanel.stepOver.addActionListener {
            step { virtualMachine.stepOver() }

            editor.requestFocusInWindow()
        }

        controlPanel.stepReturn.addActionListener {
            step { virtualMachine.stepReturn() }

            editor.requestFocusInWindow()
        }

        controlPanel.slider.addChangeListener {
            val d = delay()
            if (d < 0) {
                timer.stop()
            } else {
                timer.initialDelay = d
                timer.delay = d
                if (controlPanel.isRunning()) {
                    timer.restart()
                }
            }
            editor.requestFocusInWindow()
        }

        editor.onKeyPressed { event ->
            when (event.keyCode) {
                KeyEvent.VK_M -> if (event.isControlDown && event.isShiftDown) {
                    virtualMachinePanel.isVisible = !virtualMachinePanel.isVisible
                }

                KeyEvent.VK_F12 -> {
                    if (controlPanel.isRunning()) {
                        stepInto()
                    } else {
                        controlPanel.startStopReset.doClick()
                    }
                }
            }
        }

        editor.onRightClick = Consumer { lexeme ->
            if (controlPanel.problemPicker.isEnabled) {
                Problem.problems.firstOrNull {
                    it.name == lexeme
                }?.let {
                    controlPanel.problemPicker.selectedItem = it
                }
            }
        }

        defaultCloseOperation = EXIT_ON_CLOSE

        this.onWindowClosing {
            editor.autosaver.save()
        }
    }
}
