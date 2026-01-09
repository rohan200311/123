package com.google.jules.camera.editor

import java.util.Stack

data class EditorStep(
    val operation: String,
    val parameters: Map<String, Any>,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Module 5: Undo / Redo Engine
 * State-based editor stack.
 */
class EditorState {
    private val undoStack = Stack<EditorStep>()
    private val redoStack = Stack<EditorStep>()

    fun applyOperation(operation: String, params: Map<String, Any>) {
        val step = EditorStep(operation, params)
        undoStack.push(step)
        redoStack.clear() // New operation invalidates redo history
    }

    fun undo(): EditorStep? {
        if (undoStack.isNotEmpty()) {
            val step = undoStack.pop()
            redoStack.push(step)
            return if (undoStack.isNotEmpty()) undoStack.peek() else null // Return new current state or null if empty
        }
        return null
    }

    fun redo(): EditorStep? {
        if (redoStack.isNotEmpty()) {
            val step = redoStack.pop()
            undoStack.push(step)
            return step
        }
        return null
    }

    fun getCurrentState(): List<EditorStep> {
        return undoStack.toList()
    }
}
