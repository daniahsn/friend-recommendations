# 🤝 Student Project Partner Recommender

A smart Java-based tool to help students find ideal project partners based on **skills**, **majors**, and **past collaborations**. Includes an interactive graph view, styled recommendations, and a modern UI.

---

## 🔍 Features


| Feature | Description |
|--------|-------------|
| 🎛 Weighted Matching | Tune weight importance dynamically for majors, skills, and collaboration history |
| 📈 Skill bar visualization | Frontend, backend, and design skill level comparison |
| 🕸️ Graph-based UI | Understand existing collaboration networks |
| ⚙️ Configurable via form | Easy to scale and adapt |
| 🖼️ Clean, responsive Swing-based UI | Consistent fonts, colors, and spacing |
| 📤 Export-ready | Output teams to CSV (planned) |

---

## 🚀 How It Works

1. Students fill out a **Google Form** with:
   - Name & major
   - Skill ratings (1–5)
   - Past collaborators

2. CSVs are loaded into the app using `StudentDataLoader`.

3. The app computes **top 5 partner recommendations** and displays:
   - Skill breakdowns
   - Similarity scores
   - Collaboration graph

4. Instructors or students can visually explore connections and use the tool to form balanced teams.

---

## 🧠 Tech Stack

- Java 17+, Swing
- HTML-styled panels (JTextPane)
- CSV input (students, skills, collaborations)
- Google Sheets compatible (manual or API-based)

---

## 🧑‍🏫 Use Cases

- **Instructors**: Assign groups while balancing collaboration history and skill diversity
- **Students**: Find ideal partners based on interests and availability
- **Clubs/Hackathons**: Quickly form teams with complementary strengths
- **Course Staff**: Identify isolated students via graph insights

## 💡 Future Ideas

| Feature | Description |
|--------|-------------|
| 🧾 Google Sheets Sync | Auto-fetch live responses from Google Forms |
| 👥 Group Builder | Let students or instructors form full teams |
| 🗂️ Instructor View | Manage ungrouped students, edit groups |
| 🌐 Web Version | Rebuild in React + Spring Boot for deployment |
| 🧠 AI Matching | Use clustering or ML to suggest optimal teamings |

---

## 📝 Author

Built by [Daniyah Hasan](https://www.linkedin.com/in/daniyah-hasan)  
Use it, adapt it, or extend it for your course or club!

---

## 📄 License

MIT – free to use and modify.