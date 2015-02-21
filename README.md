## Battleship
**Battleship** is a university project. Instead of using pen and paper, we used [Chord](https://de.wikipedia.org/wiki/Chord), more specifially the [OpenChord implementation for Java](http://open-chord.sourceforge.net/), to play.
This means that we devided our part of the Chord DHT ring into *I* intervalls and put *S* ships there.
We then receive broadcasts on a location and see will look up if the other user sent `retrieve()` in one of our intervalls.
It's then our turn to send a `retrieve()`. The user who first "kills" another user and notices it, wins.

### More information
* http://inet.cpt.haw-hamburg.de/teaching/ws-2014-15/technik-und-technologie-vernetzter-systeme
