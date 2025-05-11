document.addEventListener('DOMContentLoaded', function() {
    // 강의 카드 클릭 처리 - 강의 상세 페이지로 이동
    const lectureCards = document.querySelectorAll('.lecture-card');

    lectureCards.forEach(card => {
        card.addEventListener('click', function(event) {
            // 버튼 클릭은 이벤트 버블링 방지 (이미 자체 이벤트 핸들러가 있음)
            if (event.target.closest('.lecture-actions')) {
                return;
            }

            const lectureId = this.dataset.lectureId;
            window.location.href = `/lectures/${lectureId}`;
        });
    });

    // 추천 이유와 기대 효과 토글 기능
    const toggleHeadings = document.querySelectorAll('.recommendation-reason h4, .expected-effect h4');

    toggleHeadings.forEach(heading => {
        // 초기 상태 - 내용 보이기
        const content = heading.nextElementSibling;
        content.style.display = 'block';
        heading.classList.add('expanded');

        // 토글 기능 추가
        heading.addEventListener('click', function() {
            const content = this.nextElementSibling;

            if (content.style.display === 'none') {
                content.style.display = 'block';
                this.classList.add('expanded');
            } else {
                content.style.display = 'none';
                this.classList.remove('expanded');
            }
        });
    });

    // 반응형 레이아웃을 위한 윈도우 리사이즈 핸들러
    function adjustLayout() {
        const recommendationContainer = document.querySelector('.recommendation-container');

        if (window.innerWidth < 768) {
            // 모바일 레이아웃 조정
            recommendationContainer.classList.add('mobile-view');
        } else {
            // 데스크탑 레이아웃 복원
            recommendationContainer.classList.remove('mobile-view');
        }
    }

    // 초기 레이아웃 조정 및 리사이즈 이벤트 리스너 등록
    adjustLayout();
    window.addEventListener('resize', adjustLayout);
});