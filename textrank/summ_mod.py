from summa import summarizer
from summa import keywords

orig_text = """Automatic summarization is the process of reducing a text document with a computer program in order to create a summary that retains the most important points of the original document .
\nAs the problem of information overload has grown , and as the quantity of data has increased , so has interest in automatic summarization .
\nTechnologies that can make a coherent summary take into account variables such as length , writing style and syntax .
\nAn example of the use of summarization technology is search engines such as Google .
\nDocument summarization is another ."""
text = orig_text.split('\n')
text = [sent.split() for sent in text]

# Summary:
print(summarizer.summarize(text, ratio=0.5))

# Keywords:
print(keywords.keywords(orig_text))