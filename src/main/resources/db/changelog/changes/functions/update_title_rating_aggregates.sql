CREATE OR REPLACE FUNCTION update_title_rating_aggregates()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        UPDATE titles
        SET 
            user_rating_avg = (
                (user_rating_avg * user_rating_count + NEW.score) / 
                (user_rating_count + 1)
            ),
            user_rating_count = user_rating_count + 1
        WHERE id = NEW.title_id;
        RETURN NEW;
    ELSIF TG_OP = 'UPDATE' THEN
        UPDATE titles
        SET 
            user_rating_avg = (
                (user_rating_avg * user_rating_count - OLD.score + NEW.score) / 
                user_rating_count
            )
        WHERE id = NEW.title_id;
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        UPDATE titles
        SET 
            user_rating_avg = CASE 
                WHEN user_rating_count > 1 THEN
                    (user_rating_avg * user_rating_count - OLD.score) / 
                    (user_rating_count - 1)
                ELSE 0
            END,
            user_rating_count = user_rating_count - 1
        WHERE id = OLD.title_id;
        RETURN OLD;
    END IF;
    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;
