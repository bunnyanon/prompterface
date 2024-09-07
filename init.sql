drop table if exists chats;
drop table if exists tags;
drop table if exists characters;
drop table if exists TagCharacter;
drop table if exists messages;


create table chats (
  id INTEGER PRIMARY KEY,
  type INTEGER
);

create table tags (
  id TEXT PRIMARY KEY
);
  
create table characters (
  id INTEGER PRIMARY KEY,
  name TEXT,
  defs TEXT,
  
  chat_id INTEGER,
  FOREIGN KEY(chat_id) REFERENCES chats(id)
);

create table TagCharacter (
  id INTEGER PRIMARY KEY,
  tag_id TEXT,
  character_id INTEGER,
  FOREIGN KEY(tag_id) REFERENCES tags(id),
  FOREIGN KEY(character_id) REFERENCES characters(id)
);
  
create table messages (
  id INTEGER PRIMARY KEY,
  content TEXT,
  type INTEGER,
  
  chat_id INTEGER,
  character_id INTEGER,
  
  FOREIGN KEY(chat_id) REFERENCES chats(id),
  FOREIGN KEY(character_id) REFERENCES characters(id)
);
