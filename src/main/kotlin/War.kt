import java.util.*
import kotlin.collections.ArrayList
import kotlin.time.ExperimentalTime

enum class Suit { HEART, SPADE, DIAMOND, CLUB }
data class Card(val rank: Int, val suit: Suit)

class War {
    val deck = createDeck()

    private fun createDeck(): List<Card> {
        val deck = mutableListOf<Card>()
        for (i in 2..14) {
            deck.add(Card(i, Suit.HEART))
            deck.add(Card(i, Suit.CLUB))
            deck.add(Card(i, Suit.SPADE))
            deck.add(Card(i, Suit.DIAMOND))
        }
        deck.shuffle()
        return deck.toList()
    }

}

@ExperimentalTime
fun main() {
    val superResults = List(1_000){
        val x = War()
        val ty = PlayerSimple(x.deck.subList(0, 26).toMutableList())
        val dad = PlayerWithDiscard(x.deck.subList(26, 52).toMutableList())
        runLoop(ty, dad)
    }


    val p1Won = superResults.count { it.player1Win }
    val p2Won = superResults.count { ! it.player1Win }
    val avgRuns = superResults.sumBy { it.turns }/superResults.size
    val max = superResults.maxByOrNull { it.turns }
    val maxDeeep = superResults.maxByOrNull { it.depth }
    println("P1 won $p1Won")
    println("P2 won $p2Won")
    println("averageRuns = $avgRuns")
    println("maxRunPlayer1win = ${max?.player1Win}")
    println("maxRun = ${max?.turns}")
    println("maxDeep = ${maxDeeep?.depth}")



}

data class FinalResult(val turns: Int, val player1Win: Boolean, val depth: Int)


interface Player{
    fun hasLessThan(x: Int): Boolean
    fun draw(): Card
    fun addWinnings(wins: List<Card>)
}

class PlayerSimple(private val hand: MutableList<Card>): Player{
    override fun hasLessThan(x: Int): Boolean{
        return hand.size < x
    }
    override fun draw(): Card{
        return hand.removeFirst()
    }
    override fun addWinnings(wins: List<Card>){
        hand.addAll(wins.shuffled())
    }
}

class PlayerWithDiscard(hand: MutableList<Card>): Player{
    val currentHand = hand.toMutableList()
    val discard = mutableListOf<Card>()
    override fun hasLessThan(x: Int): Boolean{
        return (currentHand.size + discard.size) < x
    }
    override fun draw(): Card{
        if(currentHand.isEmpty()){
            currentHand.addAll(discard.shuffled())
            discard.clear()
        }
        return currentHand.removeFirst()
    }
    override fun addWinnings(wins: List<Card>){
        discard.addAll(wins)
    }
}


@ExperimentalTime
private fun runLoop(p2: Player, p1: Player): FinalResult {
    var turns = 0
    var maxDepth = 0
    while (true) {
        if (p2.hasLessThan(1)) {
            return FinalResult(turns, true, maxDepth)
        }
        if (p1.hasLessThan(1)) {
            return FinalResult(turns, false, maxDepth)
        }

        val p1Card = p1.draw()
        val p2Card = p2.draw()
        val pot = mutableListOf(p1Card, p2Card)

        if(p2Card.rank > p1Card.rank){
            p2.addWinnings(pot)
        }else if(p2Card.rank < p1Card.rank){
            p1.addWinnings(pot)
        }else{

            val (winnings, player1Win, newDeep) = war(p1, p2, pot,1)
            if(newDeep > maxDepth){
                maxDepth= newDeep
            }
            if(player1Win){
                p1.addWinnings(winnings)
            }else{
                p2.addWinnings(winnings)
            }
        }

        turns++
    }
}

data class WarResult(val winnings: MutableList<Card>, val player1Win: Boolean, val depth: Int)

fun war(p1: Player, p2: Player, winnings: MutableList<Card>, depth: Int) : WarResult {
    if (p2.hasLessThan(2)) {
        return WarResult(winnings, true, depth)
    }
    if (p1.hasLessThan(2)) {
        return WarResult(winnings, false, depth)
    }

    val p1Down = p1.draw()
    val p1Up = p1.draw()
    val p2Down = p2.draw()
    val p2Up = p2.draw()

    winnings.add(p1Up)
    winnings.add(p1Down)
    winnings.add(p2Up)
    winnings.add(p2Down)

    if(p1Up.rank > p2Up.rank){
        return WarResult(winnings, true,depth)
    }else if(p1Up.rank < p2Up.rank){
        return WarResult(winnings, false,depth)
    }else{
        return war(p1, p2, winnings,depth+1)
    }
}
