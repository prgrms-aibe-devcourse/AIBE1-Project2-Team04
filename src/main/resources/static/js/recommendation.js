document.addEventListener('DOMContentLoaded', function() {
    // 강의 카드 클릭 처리
    const lectureCards = document.querySelectorAll('.lecture-card');

    lectureCards.forEach(card => {
        card.addEventListener('click', function() {
            const lectureId = this.dataset.lectureId;
            window.location.href = `/lectures/${lectureId}`;
        });
    });

    // 추천 이유 토글 처리
    const reasonTitles = document.querySelectorAll('.recommendation-reason h3');

    reasonTitles.forEach(title => {
        title.addEventListener('click', function() {
            const content = this.nextElementSibling;

            if (content.style.display === 'none' || content.style.display === '') {
                content.style.display = 'block';
                this.classList.add('expanded');
            } else {
                content.style.display = 'none';
                this.classList.remove('expanded');
            }
        });
    });
});