Play:
  >Set all players to 5000
  >Auto small blind and big blind
    >Dealer+1 -= 100
    >Dealer+2 -= 200
  >First Bets
    >start at Dealer + 3
      >Fold
        >Money stays same
        >Exits round
      >Call
        >Matches previous bet
        >Money -= bet
      >Raise
        >Set bet to input (increments of 100, higher than previous bet)
        >Money -= input
      >Check
        >only if current highest - current bet = 0
        >No bet or change
      >End round
        >Back to Dealer + 2
        >if current highest = big blind
          >check or Raise
        >if current highest > big blind
          >fold, call, or raise
  >Next Bets
    >start at Dealer + 1
      >Fold
        >Money stays same
        >Exits round
      >Call
        >Matches previous bet
        >Money -= bet
      >Raise
        >Set bet to input (increments of 100, higher than previous bet)
        >Money -= input
      >Check
        >only if current highest - current bet = 0
        >No bet or change
  >Weird betting
    >Separate pot
      >Happens if current highest bet > highest possible bet for the betting players
        >create new pot
        >pot1 = all bets up to highest possible by lowest playing player
