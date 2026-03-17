# Configuration file for the Sphinx documentation builder.
# DevOps Cookbook - Bilingual (EN/ZH) - Sphinx + MyST + Mermaid

project = 'DevOps Cookbook'
copyright = '2026, Walter Fan'
author = 'Walter Fan'
release = '1.0.0'

# -- General configuration ---------------------------------------------------
extensions = [
    'myst_parser',
    'sphinxcontrib.mermaid',
    'sphinx.ext.autodoc',
    'sphinx.ext.viewcode',
    'sphinx.ext.todo',
    'sphinx_copybutton',
    'sphinx_design',  # for grid cards on landing page
]

# MyST configuration
myst_enable_extensions = [
    "colon_fence",
    "deflist",
    "fieldlist",
    "html_admonition",
    "html_image",
    "replacements",
    "smartquotes",
    "strikethrough",
    "substitution",
    "tasklist",
]
myst_heading_anchors = 3

# Mermaid configuration - use CDN for rendering
mermaid_version = "11"
mermaid_init_js = ""

# Source settings
source_suffix = {
    '.rst': 'restructuredtext',
    '.md': 'markdown',
}
templates_path = ['_templates']
exclude_patterns = ['chapters']  # old single-language dir, now using en/ and zh/

# -- Options for HTML output -------------------------------------------------
html_theme = 'sphinx_book_theme'
html_static_path = ['_static']
html_title = 'DevOps Cookbook'
html_theme_options = {
    "repository_url": "https://github.com/walterfan/devops-cookbook",
    "use_repository_button": True,
    "use_issues_button": True,
    "use_edit_page_button": False,
    "show_toc_level": 2,
    "navigation_with_keys": True,
}

# -- Options for todo extension ----------------------------------------------
todo_include_todos = True

# Suppress warnings for missing references
suppress_warnings = ["myst.header"]
