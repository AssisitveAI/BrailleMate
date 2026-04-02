#!/usr/bin/env python3
"""
2024 한국 점자 규정 전문 텍스트를 구조화된 JSON으로 파싱하는 스크립트.
출력: app/src/main/assets/regulations.json
"""
import json
import re
import os

def parse_regulations(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        text = f.read()
    
    lines = text.split('\n')
    
    # Define main sections and their line ranges (approximate)
    sections_data = []
    
    # We'll parse the document structure
    current_section = None
    current_chapter = None
    current_subsection = None
    current_article = None
    current_content_lines = []
    current_examples = []
    current_notes = []
    article_order = 0
    
    # Track what major section we're in
    major_sections = [
        "한국 점자 표기의 기본 원칙",
        "한글 점자",
        "수학 점자", 
        "과학 점자",
        "한국 음악 점자",
        "서양 음악 점자",
        "[부록 1] 외국어 점자",
        "[부록 2] 국제음성기호 점자"
    ]
    
    all_articles = []
    
    i = 0
    in_toc = True  # We start in table of contents
    
    while i < len(lines):
        line = lines[i].strip().replace('\r', '')
        
        # Skip empty lines in some contexts
        if not line:
            i += 1
            continue
        
        # Detect end of table of contents
        if in_toc and line == "한국 점자 표기의 기본 원칙":
            # Check if this is the actual content (not TOC)
            if i > 100:  # After TOC
                in_toc = False
                current_section = "한국 점자 표기의 기본 원칙"
                current_chapter = "기본 원칙"
                i += 1
                continue
        
        if in_toc:
            i += 1
            continue
        
        # Detect major section headers
        for sec in major_sections:
            if line == sec and current_section != sec:
                # Save current article if exists
                if current_article:
                    save_article(all_articles, current_section, current_chapter, 
                               current_subsection, current_article, 
                               current_content_lines, current_examples, current_notes, article_order)
                    article_order += 1
                    current_article = None
                    current_content_lines = []
                    current_examples = []
                    current_notes = []
                
                current_section = sec
                current_chapter = sec
                current_subsection = None
                break
        
        # Detect chapter headers: "제X장 ..."
        chapter_match = re.match(r'^(제\d+장\s+.+)$', line)
        if chapter_match:
            if current_article:
                save_article(all_articles, current_section, current_chapter,
                           current_subsection, current_article,
                           current_content_lines, current_examples, current_notes, article_order)
                article_order += 1
                current_article = None
                current_content_lines = []
                current_examples = []
                current_notes = []
            
            current_chapter = chapter_match.group(1).strip()
            current_subsection = None
            i += 1
            continue
        
        # Detect subsection headers: "제X절 ..."
        subsection_match = re.match(r'^(제\d+절\s+.+)$', line)
        if subsection_match:
            if current_article:
                save_article(all_articles, current_section, current_chapter,
                           current_subsection, current_article,
                           current_content_lines, current_examples, current_notes, article_order)
                article_order += 1
                current_article = None
                current_content_lines = []
                current_examples = []
                current_notes = []
            
            current_subsection = subsection_match.group(1).strip()
            i += 1
            continue
        
        # Detect article headers: "제X항 ..."
        article_match = re.match(r'^(제\d+항)\s+(.+)$', line)
        if article_match:
            if current_article:
                save_article(all_articles, current_section, current_chapter,
                           current_subsection, current_article,
                           current_content_lines, current_examples, current_notes, article_order)
                article_order += 1
            
            current_article = article_match.group(1)
            current_content_lines = [article_match.group(2)]
            current_examples = []
            current_notes = []
            i += 1
            continue
        
        # Also match standalone "제X항" 
        article_match2 = re.match(r'^(제\d+항)$', line)
        if article_match2:
            if current_article:
                save_article(all_articles, current_section, current_chapter,
                           current_subsection, current_article,
                           current_content_lines, current_examples, current_notes, article_order)
                article_order += 1
            
            current_article = article_match2.group(1)
            current_content_lines = []
            current_examples = []
            current_notes = []
            i += 1
            continue
        
        # Detect notes: [다만], [붙임]
        if line.startswith('[다만') or line.startswith('[붙임'):
            current_notes.append(line)
            i += 1
            continue
        
        # If we're in an article, collect content
        if current_article:
            current_content_lines.append(line)
        
        i += 1
    
    # Save last article
    if current_article:
        save_article(all_articles, current_section, current_chapter,
                   current_subsection, current_article,
                   current_content_lines, current_examples, current_notes, article_order)
    
    # Group into sections structure
    sections = {}
    for art in all_articles:
        sec = art['section']
        chap = art['chapter']
        if sec not in sections:
            sections[sec] = {}
        if chap not in sections[sec]:
            sections[sec][chap] = []
        sections[sec][chap].append(art)
    
    # Convert to final format
    result = []
    for sec_name in major_sections:
        if sec_name in sections:
            chapters = []
            for chap_name, articles in sections[sec_name].items():
                chapter_articles = []
                for art in articles:
                    article_data = {
                        "articleNumber": art['articleNumber'],
                        "content": art['content'],
                    }
                    if art.get('subSection'):
                        article_data['subSection'] = art['subSection']
                    if art.get('examples'):
                        article_data['examples'] = art['examples']
                    if art.get('notes'):
                        article_data['notes'] = art['notes']
                    chapter_articles.append(article_data)
                
                chapters.append({
                    "chapter": chap_name,
                    "articles": chapter_articles
                })
            
            result.append({
                "section": sec_name,
                "chapters": chapters
            })
    
    return result


def save_article(articles_list, section, chapter, subsection, article_num, 
                 content_lines, examples, notes, order):
    # Clean content - remove braille notation lines (mostly ASCII art)
    clean_content = []
    for line in content_lines:
        # Skip lines that look like pure braille notation
        if line and not is_braille_notation(line):
            clean_content.append(line)
    
    content = ' '.join(clean_content).strip()
    if not content:
        content = f"{article_num} (내용은 점자 표기 참조)"
    
    # Parse examples from corpus data if available
    article_examples = []
    
    articles_list.append({
        'section': section or "기타",
        'chapter': chapter or "기타",
        'subSection': subsection,
        'articleNumber': article_num,
        'content': content,
        'examples': article_examples if article_examples else None,
        'notes': '\n'.join(notes) if notes else None,
        'orderIndex': order
    })


def is_braille_notation(line):
    """Check if a line is likely pure braille ASCII notation"""
    # Braille notation lines typically contain mostly special characters
    if not line.strip():
        return True
    
    # Lines with backticks are typically braille
    if line.startswith('``') or line.startswith('  ``'):
        return True
    
    # Very short lines that look like braille patterns
    korean_chars = sum(1 for c in line if '\uac00' <= c <= '\ud7a3' or '\u3131' <= c <= '\u3163')
    total_chars = len(line.strip())
    
    if total_chars > 0 and total_chars <= 10 and korean_chars == 0:
        return True
    
    return False


def merge_corpus_examples(regulations_data, corpus_path):
    """Merge examples from the extracted corpus JSON"""
    if not os.path.exists(corpus_path):
        return regulations_data
    
    with open(corpus_path, 'r', encoding='utf-8') as f:
        corpus = json.load(f)
    
    # Group corpus examples by category
    examples_by_category = {}
    for item in corpus:
        cat = item.get('category', '')
        if cat not in examples_by_category:
            examples_by_category[cat] = []
        examples_by_category[cat].append({
            'text': item['text'],
            'braille': item['braille']
        })
    
    # Assign examples to matching sections
    for section in regulations_data:
        sec_name = section['section']
        matching_examples = examples_by_category.get(sec_name, [])
        
        if matching_examples and section['chapters']:
            # Distribute examples across articles
            for chapter in section['chapters']:
                for article in chapter['articles']:
                    # Try to find related examples by matching text in content
                    related = []
                    for ex in matching_examples:
                        if ex['text'] in article['content']:
                            related.append(ex)
                    
                    if related:
                        article['examples'] = related[:10]  # Limit to 10 examples
            
            # If first chapter has no examples, add some general ones
            if section['chapters'] and section['chapters'][0]['articles']:
                first_article = section['chapters'][0]['articles'][0]
                if not first_article.get('examples') and matching_examples:
                    first_article['examples'] = matching_examples[:20]
    
    return regulations_data


if __name__ == '__main__':
    script_dir = os.path.dirname(os.path.abspath(__file__))
    project_root = script_dir
    
    regulation_file = os.path.join(project_root, 'references', '2024_standard', 
                                    '[개정]+한국+점자+규정+전문.txt')
    corpus_file = os.path.join(project_root, 'references', '2024_standard',
                                'temp_extracted_corpus.json')
    output_file = os.path.join(project_root, 'app', 'src', 'main', 'assets', 
                                'regulations.json')
    
    print("Parsing regulation text...")
    data = parse_regulations(regulation_file)
    
    print("Merging corpus examples...")
    data = merge_corpus_examples(data, corpus_file)
    
    # Statistics
    total_articles = sum(
        len(article) 
        for section in data 
        for chapter in section['chapters']
        for article in [chapter['articles']]
    )
    
    print(f"\nParsed {len(data)} sections:")
    for section in data:
        article_count = sum(len(ch['articles']) for ch in section['chapters'])
        print(f"  - {section['section']}: {len(section['chapters'])} chapters, {article_count} articles")
    
    os.makedirs(os.path.dirname(output_file), exist_ok=True)
    with open(output_file, 'w', encoding='utf-8') as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
    
    print(f"\nOutput saved to: {output_file}")
    print(f"File size: {os.path.getsize(output_file) / 1024:.1f} KB")
