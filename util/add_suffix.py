#!/usr/bin/env python3

import sys, pyperclip

def add_suffix(text, suffix):
  
    lines = text.split("\n")

    for i in range(len(lines)):
        lines[i] = lines[i] + suffix

    text = '\n'.join(lines)

    return text


if __name__ == '__main__':
    if len(sys.argv) < 2:
        print('usage: python add_prefix <prefix> - add prefix to the text in clipboard')
        sys.exit(1)

    suffix = sys.argv[1]
    text = pyperclip.paste()
    print("copied text to clipboard:\n{}\n{}\n".format('-'*20,text))
    text = add_suffix(text, suffix)
    print("copied text from clipboard:\n{}\n{}\n".format('-'*20,text))
