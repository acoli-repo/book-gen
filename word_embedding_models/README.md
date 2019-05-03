# Find and join n-grams as tokens
Read text files and identify frequent bigrams, replace with joined tokens (e.g., New York -> New_York) and save into single file:

<code>python find_phrases.py <input file 1> [<input file 2> ...] <output file> </code>
  
Repeat to identify frequent trigrams etc. (New_York Times -> New_York_Times).

# Train word vectors with n-grams

Train word vectors on corpus with frequent n-grams replaced, with: <code>python train_w2v.py <input files/dirs> </code>

Supports reading files or directories with .txt files (see code).


