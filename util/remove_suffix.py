#!/usr/bin/env python3

import sys, pyperclip

def remove_suffix(text, suffix):
  
    lines = text.split("\n")

    for i in range(len(lines)):
        text = lines[i]
        if text.endswith(suffix):            
            lines[i] = text[:-len(suffix)]
        else:
            lines[i] = text[:]

    text = '\n'.join(lines)

    return text


if __name__ == '__main__':
    if len(sys.argv) < 2:
        print('usage: python add_prefix <prefix> - add prefix to the text in clipboard')
        sys.exit(1)

    prefix = sys.argv[1]
    text = pyperclip.paste()
    print("remove prefix from clipboard:\n{}\n{}\n".format('-'*40,text))
    text = remove_suffix(text, prefix)
    print("remove prefix from clipboard:\n{}\n{}\n".format('-'*40,text))
