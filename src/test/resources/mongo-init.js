db = new Mongo().getDB("a6-projectpresentations");

db.createCollection('projectspresentations', { capped: false });

db.projectspresentations.insert([
    { "item": 1 },
    { "item": 2 },
    { "item": 3 },
    { "item": 4 },
    { "item": 5 }
]);