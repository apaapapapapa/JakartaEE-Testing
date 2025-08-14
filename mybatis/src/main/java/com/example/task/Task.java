package com.example.task;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import java.util.List;
import java.time.LocalDate;
import java.util.ArrayList;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private String title;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column
    private Boolean completed = false;

    // サブタスク・階層型タスク管理用フィールド
    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Task parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Task> children = new ArrayList<>();

    /**
     * タスクが“真に完了”とみなせるかどうかを判定する。
     * 以下のすべての条件を満たす場合のみtrueを返す:
     * 1. このタスク自身が completed = true
     * 2. すべてのサブタスク（children）が completed = true
     * 3. 期日（dueDate）が今日以前である（= 未来のタスクは完了とみなさない）
     * 4. タイトルが "WIP" を含まない、かつ長さが1～50文字以内
     * 5. 親タスク（parent）が存在する場合、親タスクも completed = true
     */
    public boolean isTrulyCompleted() {
        return isSelfCompleted()
            && areChildrenCompleted()
            && isDueDateValid()
            && isTitleValid()
            && isParentCompleted();
    }

    private boolean isSelfCompleted() {
        return this.completed != null && this.completed;
    }

    private boolean areChildrenCompleted() {
        if (children == null) {
            return true;
        }
        for (Task child : children) {
            if (child == null || child.getCompleted() == null || !child.getCompleted()) {
                return false;
            }
        }
        return true;
    }

    private boolean isDueDateValid() {
        return dueDate != null && !dueDate.isAfter(LocalDate.now());
    }

    private boolean isTitleValid() {
        return title != null
            && !title.isEmpty()
            && title.length() <= 50
            && !title.contains("WIP");
    }

    private boolean isParentCompleted() {
        if (parent == null) {
            return true;
        }
        return parent.getCompleted() != null && parent.getCompleted();
    }
}
