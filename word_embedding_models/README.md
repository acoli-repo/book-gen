# Find and join n-grams as tokens
Read text files and identify frequent bigrams, replace with joined tokens (e.g., New York -> New_York) and save into single file:

<code>python find_phrases.py &lt;input file 1&gt; [&lt;input file 2&gt; ...] &lt;output file&gt;</code>
  
The script performs word tokenization and phrase detection line by line. Recommended: one sentence per line.

Repeat to identify frequent trigrams etc. (New_York Times -> New_York_Times).

# Train word vectors with n-grams

Train word vectors on corpus with frequent n-grams replaced, with: 

<code>python train_w2v.py &lt;input files/dirs&gt;</code>

Supports reading files or directories with .txt files (see code).


