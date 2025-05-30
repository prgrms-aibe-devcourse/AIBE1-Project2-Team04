document.addEventListener('DOMContentLoaded', function() {
    // 게임 선택 페이지 처리
    const gameOptions = document.querySelectorAll('.game-option');
    if (gameOptions.length > 0) {
        const selectedGameInput = document.getElementById('selectedGame');
        const nextButton = document.querySelector('.btn-next');

        gameOptions.forEach(option => {
            option.addEventListener('click', function() {
                // 이전 선택 제거
                gameOptions.forEach(opt => opt.classList.remove('selected'));

                // 현재 선택 추가
                this.classList.add('selected');

                // 선택된 게임 저장
                selectedGameInput.value = this.dataset.game;

                // 다음 버튼 활성화
                nextButton.disabled = false;
            });
        });
    }

    // 실력 평가 페이지 처리
    const skillOptions = document.querySelectorAll('.skill-option');
    if (skillOptions.length > 0) {
        const selectedSkillInput = document.getElementById('selectedSkill');
        const nextButton = document.querySelector('.btn-next');

        skillOptions.forEach(option => {
            option.addEventListener('click', function() {
                // 이전 선택 제거
                skillOptions.forEach(opt => opt.classList.remove('selected'));

                // 현재 선택 추가
                this.classList.add('selected');

                // 선택된 실력 저장
                selectedSkillInput.value = this.dataset.skill;

                // 다음 버튼 활성화
                nextButton.disabled = false;
            });
        });
    }

    // 학습 목표 페이지 처리
    const goalOptions = document.querySelectorAll('.goal-option');
    if (goalOptions.length > 0) {
        const selectedGoalInput = document.getElementById('selectedGoal');
        const nextButton = document.querySelector('.btn-next');

        goalOptions.forEach(option => {
            option.addEventListener('click', function() {
                // 이전 선택 제거
                goalOptions.forEach(opt => opt.classList.remove('selected'));

                // 현재 선택 추가
                this.classList.add('selected');

                // 선택된 목표 저장
                selectedGoalInput.value = this.dataset.goal;

                // 다음 버튼 활성화
                nextButton.disabled = false;
            });
        });
    }

    // 가능 시간 페이지 처리
    const timeOptions = document.querySelectorAll('.time-option');
    if (timeOptions.length > 0) {
        const selectedTimeInput = document.getElementById('selectedTime');
        const nextButton = document.querySelector('.btn-next');

        timeOptions.forEach(option => {
            option.addEventListener('click', function() {
                // 이전 선택 제거
                timeOptions.forEach(opt => opt.classList.remove('selected'));

                // 현재 선택 추가
                this.classList.add('selected');

                // 선택된 시간 저장
                selectedTimeInput.value = this.dataset.time;

                // 다음 버튼 활성화
                nextButton.disabled = false;
            });
        });
    }

    // 강의 선호도 페이지 처리
    const preferenceOptions = document.querySelectorAll('.preference-option');
    if (preferenceOptions.length > 0) {
        const selectedPreferenceInput = document.getElementById('selectedPreference');
        const completeButton = document.querySelector('.btn-complete');

        preferenceOptions.forEach(option => {
            option.addEventListener('click', function() {
                // 이전 선택 제거
                preferenceOptions.forEach(opt => opt.classList.remove('selected'));

                // 현재 선택 추가
                this.classList.add('selected');

                // 선택된 선호도 저장
                selectedPreferenceInput.value = this.dataset.preference;

                // 완료 버튼 활성화
                completeButton.disabled = false;
            });
        });
    }

    // 폼 제출 시 회원 ID와 게임 티어/포지션 정보 설정
    const lecturePreferenceForm = document.getElementById('lecturePreferenceForm');
    if (lecturePreferenceForm) {
        // 현재 로그인된 회원 ID 가져오기 (없으면 기본값 1)
        const memberId = document.querySelector('meta[name="memberId"]')?.content || '1';

        // 티어와 포지션 입력 필드가 있는지 확인
        if (!document.querySelector('input[name="memberId"]')) {
            // 없으면 hidden 필드 생성
            const memberIdInput = document.createElement('input');
            memberIdInput.type = 'hidden';
            memberIdInput.name = 'memberId';
            memberIdInput.value = memberId;
            lecturePreferenceForm.appendChild(memberIdInput);
        }

        if (!document.querySelector('input[name="gameTier"]')) {
            const gameTierInput = document.createElement('input');
            gameTierInput.type = 'hidden';
            gameTierInput.name = 'gameTier';
            gameTierInput.value = '';
            lecturePreferenceForm.appendChild(gameTierInput);
        }

        if (!document.querySelector('input[name="gamePosition"]')) {
            const gamePositionInput = document.createElement('input');
            gamePositionInput.type = 'hidden';
            gamePositionInput.name = 'gamePosition';
            gamePositionInput.value = '';
            lecturePreferenceForm.appendChild(gamePositionInput);
        }
    }
});