#!/usr/bin/env python3

import sys, pyperclip

def break_by_cr(text, sep):
  
    lines = text.split(sep)

    for i in range(len(lines)):
        lines[i] = lines[i]

    text = '\n'.join(lines)

    return text


if __name__ == '__main__':
    sep = "\\r\\n"
    if len(sys.argv) > 1:
        sep = sys.argv[1]
    
    text = pyperclip.paste()
    print("copied text to clipboard:\n{}\n{}\n".format('-'*20,text))
    text = break_by_cr(text, sep)
    print("copied text from clipboard:\n{}\n{}\n".format('-'*20,text))
