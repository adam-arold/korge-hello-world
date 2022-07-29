import com.soywiz.kds.*
import com.soywiz.korge.Korge
import com.soywiz.korge.view.*
import com.soywiz.korge.view.fast.*
import com.soywiz.korim.bitmap.*
import com.soywiz.korim.color.*
import com.soywiz.korim.format.*
import com.soywiz.korio.file.std.*

val TILE_SIZE = 16
val ROWS = 1080 / TILE_SIZE
val COLS = 1920 / TILE_SIZE

suspend fun main() = Korge(width = COLS * TILE_SIZE, height = ROWS * TILE_SIZE, bgcolor = Colors["#2b2b2b"]) {
    container {
        val glyphs = resourcesVfs["cp437.png"]
            .readBitmap()
            .toBMP32()
            .apply { updateColors { if (it == Colors.BLACK) Colors.TRANSPARENT_BLACK else it } }
            .slice()
            .split(TILE_SIZE, TILE_SIZE)
            .toTypedArray()

        val emus = listOf(
            TerminalEmulatorView(COLS, ROWS, glyphs),
            TerminalEmulatorView(COLS, ROWS, glyphs),
            TerminalEmulatorView(COLS, ROWS, glyphs),
            TerminalEmulatorView(COLS, ROWS, glyphs)
        )
        emus.forEach {
            addChild(it)
        }
        val chars = CharArray(256) { it.toChar() }
        val colors =
            listOf(Colors.BLACK, Colors.BLUE, Colors.GREEN, Colors.CYAN, Colors.PURPLE, Colors.YELLOW, Colors.WHITE)
        val bgColors = colors.map { it.withA(125) }
        addUpdater {
            for (row in 1 until ROWS) {
                for (col in 0 until COLS) {
                    emus.forEach { emu ->
                        emu.setGlyph(
                            col,
                            row,
                            chars.random(),
                            colors.random(),
                            bgColors.random()
                        )
                    }

                }
            }
        }
    }
}

class TerminalEmulatorView(val columns: Int, val rows: Int, private val glyphs: Array<out BmpSlice>) : Container() {
    private val bgBitmap = Bitmap32(TILE_SIZE, TILE_SIZE, Colors.WHITE).slice()

    private val bgFSprites = FSprites(columns * rows)
    private val bgView = bgFSprites.createView(bgBitmap.bmp).addTo(this)
    private val bgMat = IntArray2(columns, rows) { bgFSprites.alloc().id }

    private val fgFSprites = FSprites(columns * rows)
    private val fgView = fgFSprites.createView(glyphs.first().bmp).addTo(this)
    private val fgMat = IntArray2(columns, rows) { fgFSprites.alloc().id }

    init {
        for (row in 0 until rows) {
            for (col in 0 until columns) {
                fgFSprites.apply {
                    val fsprite = FSprite(fgMat[col, row])
                    fsprite.x = col * TILE_SIZE.toFloat()
                    fsprite.y = row * TILE_SIZE.toFloat()
                }
                bgFSprites.apply {
                    val fsprite = FSprite(bgMat[col, row])
                    fsprite.x = col * TILE_SIZE.toFloat()
                    fsprite.y = row * TILE_SIZE.toFloat()
                    fsprite.colorMul = Colors.BLACK
                    fsprite.setTex(bgBitmap)
                }
            }
        }
    }

    fun setGlyph(x: Int, y: Int, char: Char, fgcolor: RGBA, bgcolor: RGBA) {
        bgFSprites.apply {
            FSprite(bgMat[x, y]).colorMul = bgcolor
        }
        fgFSprites.apply {
            val fsprite = FSprite(fgMat[x, y])
            fsprite.colorMul = fgcolor
            fsprite.setTex(glyphs[char.code])
        }
    }
}