// script.js

document.addEventListener('DOMContentLoaded', () => {
    const difficultyColors = {
        Easy: '#00e676',
        Medium: '#ffeb3b',
        Hard: '#ff5252'
    };

    const rows = document.querySelectorAll('#problems-table tbody tr');

    rows.forEach(row => {
        const difficultyCell = row.querySelector('.difficulty');
        const difficultyText = difficultyCell.textContent.trim();

        if (difficultyColors[difficultyText]) {
            difficultyCell.style.color = difficultyColors[difficultyText];
        }
    });
});
